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
import au.com.onegeek.respite.api.ServletTestsBase
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.bson.BSONDocument

class AuthenticationTests extends ServletTestsBase with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule

  class TestServlet(implicit val bindingModule: BindingModule) extends ScalatraServlet with Injectable

  val authServlet = new TestServlet with Authentication {
//    override val db: DefaultDB = inject[DefaultDB]

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
        "X-API-Application" -> "bill",
        "X-API-Key" -> "murray")

      get("/", headers = headers) {
        status should equal(200)
      }
    }
  }
}