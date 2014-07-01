package au.com.onegeek.respite.security

import reactivemongo.api.DefaultDB
import scala.concurrent.ExecutionContext
import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.security.SecurityUtil._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.scalatra.ScalatraServlet
import org.scalatest.FunSuiteLike
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.bson.BSONDocument
import au.com.onegeek.respite.models.ApiKey
import au.com.onegeek.respite.ServletTestsBase

class AuthenticationTests extends ServletTestsBase with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule

  object ConfigAuthStrategy extends ConfigAuthenticationStrategy {
    override implicit val keys = Map("testkey" -> ApiKey(application = "test1", description = "Test application", key = "testkey"))

  }

  class TestServlet(implicit val bindingModule: BindingModule) extends ScalatraServlet with Injectable

  val authServlet = new TestServlet with Authentication  {

  implicit val authenticationStrategy = ConfigAuthStrategy

    get("/") {
      "OK"
    }

    override def initialize(config: ConfigT) {
      super.initialize(config)
    }
  }

  addServlet(authServlet, "/*")

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

    "Return None if a revoke API is called" in {
      val headers = Map(
        "X-API-Application" -> "test1",
        "X-API-Key" -> "testkey")

      get("/", headers = headers) {
        status should equal(200)
      }
    }
  }
}