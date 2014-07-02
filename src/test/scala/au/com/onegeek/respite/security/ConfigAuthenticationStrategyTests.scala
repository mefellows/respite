package au.com.onegeek.respite.security

import reactivemongo.api.DefaultDB
import au.com.onegeek.respite.config.TestConfigurationModule
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.scalatra.ScalatraServlet
import au.com.onegeek.respite.api.ServletTestsBase
import org.scalatest.concurrent.ScalaFutures

class ConfigAuthenticationStrategyTests extends ServletTestsBase with ScalaFutures {
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

  "A ConfigAuthenticationStrategy secured servlet" should {
    "reject requests without an API Key" in {
      get("/") {
        status should equal(401)
      }
    }

    "accept requests with a valid API key" in {

    }

    "provide a RESTful API to remove keys at runtime" in {

    }
  }
}