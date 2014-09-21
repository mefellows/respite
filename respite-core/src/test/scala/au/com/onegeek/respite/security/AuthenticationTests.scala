package au.com.onegeek.respite.security

import java.security.MessageDigest

import play.api.libs.json.Json
import reactivemongo.api.DefaultDB
import scala.concurrent.ExecutionContext
import au.com.onegeek.respite.config.TestConfigurationModule

import scala.reflect.ClassTag

//import au.com.onegeek.respite.security.SecurityUtil._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.scalatra.ScalatraServlet
import org.scalatest.FunSuiteLike
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import au.com.onegeek.respite.models.{User, ApiKey}
import au.com.onegeek.respite.ServletTestsBase

class AuthenticationTests extends ServletTestsBase with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule
  val API_KEY_HEADER = "X-API-Key";
  val API_APP_HEADER = "X-API-Application";
  val validHeaders: Map[String, String] = Map(API_APP_HEADER -> "test1", API_KEY_HEADER -> "testkey")
  val validHeaders2: Map[String, String] = Map(API_APP_HEADER -> "test2", API_KEY_HEADER -> "testkey2")

  object ConfigAuthStrategy extends ConfigAuthenticationStrategy {
    override implicit var keys = Map("testkey" -> ApiKey(application = "test1", description = "Test application", key = "testkey")) ++
                                 Map("testkey2" -> ApiKey(application = "test2", description = "Test application", key = "testkey2"))

  }

  class AuthServlet(implicit val bindingModule: BindingModule, implicit val tag: ClassTag[ApiKey]) extends ScalatraServlet with Authentication with Injectable {
    protected implicit def executor: ExecutionContext = ExecutionContext.global
    override implicit val authenticationStrategy = ConfigAuthStrategy

    get("/") {
      "OK"
    }

  }

  implicit val authenticationStrategy = ConfigAuthStrategy

  val authServlet = new AuthServlet
  val authServletWithApi = new AuthServlet with AuthenticationApi

  addServlet(authServlet, "/*")
  addServlet(authServletWithApi, "/auth/*")

  "A secured servlet" should {
    "reject requests without an API Key" in {
      get("/") {
        status should equal(401)
      }
    }

    "reject requests with an invalid API Key" in {
      val headers = Map(
        "X-API-Key" -> "somekey",
        "X-API-Application" -> "someapplication")

      get("/", headers = headers) {
        status should equal(401)
      }
    }

    "accept requests with a valid API Key" in {
      val headers = Map(
        "X-API-Application" -> "test1",
        "X-API-Key" -> "testkey")

      get("/", headers = headers) {
        status should equal(200)
      }
    }

    "Serialise to/from a sane JSON format" in {
      val key = new ApiKey(id = BSONObjectID("53de57cc0100000100f8fa20"), application = "hacker", description = "news for hackers", key = "1234")
      println(Json.toJson(key))

      val json = "{\"id\":{\"$oid\":\"53de57cc0100000100f8fa20\"},\"application\":\"hacker\",\"description\":\"news for hackers\",\"key\":\"1234\"}"
      println(Json.parse(json).validate[ApiKey])
      val key2: ApiKey = Json.parse(json).validate[ApiKey].get
      println(key2)
      key.id.stringify should equal(key2.id.stringify)
      key should equal (key2)
    }

    "Generate a default key" in {
      ApiKey.generateKey("foo", "bar") should equal("3858f62230ac3c915f300c664312c63f")
    }

  }

  "An AuthenticationApi Servlet secured with ConfigAuthenticationStrategy" should {

    "Provide a RESTful API to list keys at runtime" in {
      get("/auth/tokens/", headers = validHeaders) {
        status should equal (200)
        println(body)
      }
    }

    "Provide a RESTful API to remove keys at runtime" in {
      delete("/auth/tokens/testkey2", headers = validHeaders) {
        println(body)
        status should equal (200)

        body should include ("\"application\":\"test2\",\"description\":\"Test application\",\"key\":\"testkey2\"}")
      }

      // Key deleted, I should be rejected!
      get("/", headers = validHeaders2) {
        status should equal (401)
      }
    }

    "Provide a RESTful API to remove keys at runtime - return 404 if key doesn't exist" in {
      delete("/auth/tokens/key", headers = validHeaders) {
        status should equal (404)
      }
    }

    "Provide a RESTful API to remove keys at runtime - return 405 if key not provided" in {
      delete("/auth/tokens/", headers = validHeaders) {
        status should equal (405)
      }
    }

    "Provide an API to create tokens at runtime" in {
      val json = "{\"application\":\"hacker\",\"description\":\"news for hackers\",\"key\":\"1234\"}"

      post("/auth/tokens/", json.toString, validHeaders ++ Map("Content-Type" -> "application/json")) {
        println(s"heres my body: ${body}")
        status should equal(200)
        body should include("\"application\":\"hacker\"")
      }
    }

    "Provide an API to create tokens at runtime - return 400 if key exists" in {
      val json = "{\"application\":\"test1\",\"description\":\"Test application\",\"key\":\"testkey\"}"

      post("/auth/tokens/", json.toString, validHeaders ++ Map("Content-Type" -> "application/json")) {
        println(s"heres my body: ${body}")
        status should equal(400)
      }
    }

    "Provide an API to create tokens at runtime - Reject invalid token creation requests" in {
      val json = "{\"application\":\"hacker\"}"

      post("/auth/tokens/", json.toString, validHeaders ++ Map("Content-Type" -> "application/json")) {
        println(s"heres my body: ${body}")
        status should equal(400)
        body should include("{\"obj.description\":[{\"msg\":\"error.path.missing\"")
      }
    }
  }
}