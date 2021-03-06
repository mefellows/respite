package au.com.onegeek.respite.security

import au.com.onegeek.respite.config.TestConfigurationModule
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.scalatra.ScalatraServlet
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

import uk.gov.hmrc.mongo._
import play.api.libs.json.{Json, JsValue}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.MongoConnector
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.indexes.Index
import au.com.onegeek.respite.models.ApiKey
import au.com.onegeek.respite.ServletTestsBase
import uk.gov.hmrc.mongo.ReactiveMongoFormats._
import au.com.onegeek.respite.models.ModelJsonExtensions._

class DatabaseAuthenticationStrategyTests extends ServletTestsBase with ScalaFutures with MongoSpecSupport with Awaiting {
  implicit val bindingModule = TestConfigurationModule

  class TestServlet(implicit val bindingModule: BindingModule) extends ScalatraServlet with Injectable

  val repository = new ApiKeyRepository
  val API_KEY_HEADER = "X-API-Key";
  val API_APP_HEADER = "X-API-Application";
  val validHeaders: Map[String, String] = Map(API_APP_HEADER -> "application-name", API_KEY_HEADER -> "key")

  class TestAuthServlet extends AuthServlet {
    protected implicit def executor: ExecutionContext = ExecutionContext.global
    override implicit val authenticationStrategy = new DatabaseAuthenticationStrategy(repository)

    get("/") {
      "OK"
    }
  }

  class TestApiAuthServlet extends MongoDatabaseAuthServlet(repository)

  val authServlet = new TestAuthServlet
  val authServletWithApi = new TestApiAuthServlet

  before {
    // Clear out entries
    await(repository.removeAll)

    // Add some keys to test against
    val key = ApiKey(id = BSONObjectID.generate, application = "application-name", description = "my description", key = "key")
    await(repository.insert(key))

  }

  addServlet(authServlet, "/*")
  addServlet(authServletWithApi, "/auth/*")

  "A DatabaseAuthenticationStrategy secured servlet" should {
    "reject requests without an API Key" in {
      get("/") {
        status should equal(401)
      }
    }

    "accept requests with a valid API Key" in {
      get("/", headers = validHeaders) {
        status should equal (200)
        body should equal ("OK")
      }
    }

    "Store stuff in repo" in {

      val e1 = ApiKey(application = "application-name1", description = "my description", key = "key1")
      val e2 = ApiKey(application = "application-name2", description = "my description", key = "key2")
      val e3 = ApiKey(application = "application-name3", description = "my description", key = "key3")
      val e4 = ApiKey(application = "application-name4", description = "my description", key = "key4")

      println(Json.toJson(e1).toString())

      val created = for {
        res1 <- repository.save(e1)
        res2 <- repository.save(e2)
        res3 <- repository.save(e3)
        countResult <- repository.count
      } yield countResult

      await(created) shouldBe 4

      val result: List[ApiKey] = await(repository.findAll)
      result.foreach (println)
      result.size shouldBe 4
      result should contain(e1)
      result should contain(e2)
      result should contain(e3)

      result should not contain (e4)
    }
  }

  "A Servlet secured with DatabaseAuthenticationStrategy with AuthenticationApi" should {

    "Provide a RESTful API to remove keys at runtime" in {
      delete("/auth/tokens/key", headers = validHeaders) {
        status should equal (200)
        body should include ("\"application\":\"application-name\",\"description\":\"my description\",\"key\":\"key\"}")
      }

      // Key deleted, I should be rejected!
      get("/", headers = validHeaders) {
        status should equal (401)
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

    "Reject invalid token creation requests" in {
      val json = "{\"application\":\"hacker\"}"

      post("/auth/tokens/", json.toString, validHeaders ++ Map("Content-Type" -> "application/json")) {
        println(s"heres my body: ${body}")
        status should equal(400)
        body should include("{\"obj.description\":[{\"msg\":\"error.path.missing\"")
      }
    }

    "List all keys" in {
      get("/auth/tokens/", headers = validHeaders) {
        status should equal (200)
        body should include ("\"application\":\"application-name\",\"description\":\"my description\",\"key\":\"key\"}]")
      }
    }

    "Reject a request with incorrect key" in {
      delete("/auth/tokens/notexist", headers = validHeaders) {
        status should equal (404)
      }
    }
  }

  "An Servlet secured with DatabaseAuthenticationStrategy with a slow / unavailable Database" should {

    "Respond with a 503" in {
      mongoConnectorForTest.close()
      // Key deleted, I should be rejected!
      get("/", headers = validHeaders) {

        println(body)
        println(status)
        status should equal (503)
      }
    }
  }
}