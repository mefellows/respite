package au.com.onegeek.respite.security

import au.com.onegeek.respite.config.TestConfigurationModule
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.scalatra.ScalatraServlet
import au.com.onegeek.respite.api.ServletTestsBase
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.api.DefaultDB

class DatabaseAuthenticationStrategyTests extends ServletTestsBase with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule

  class TestServlet(implicit val bindingModule: BindingModule) extends ScalatraServlet with Injectable

  val authServlet = new TestServlet with Authentication {
    override val db: DefaultDB = inject[DefaultDB]

    get("/") {
      "OK"
    }

    override def initialize(config: ConfigT) {
      super.initialize(config)
    }
  }

  addServlet(authServlet, "/*")

  "A DatabaseAuthenticationStrategy secured servlet" should {
    "reject requests without an API Key" in {
      get("/") {
        status should equal(401)
      }
    }

    "accept requests with a valid API Key" in {

    }

    "provide a RESTful API to remove keys at runtime" in {

    }

  }
}