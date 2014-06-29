package au.com.respite.api.controllers

import au.com.respite.api.config.TestConfigurationModule
import reactivemongo.api.MongoDriver
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext
import au.com.respite.api.ServletTestsBase

// Do I need these or not? Seems to work without...
//import org.scalatest.junit.JUnitRunner
//import org.junit.runner.RunWith
//@RunWith(classOf[JUnitRunner])
class RestControllerSpec extends ServletTestsBase {
  protected implicit def executor: ExecutionContext = ExecutionContext.global
  implicit val bindingModule = TestConfigurationModule

  addServlet(new UsersController, "/api/*")

  "A RestController" should {

    get("/api/") {
      status should equal (200)
      body should include ("Matt")
    }

    get("/api/1") {
      status should equal (200)
      body should include ("Matt")
    }

    put("/api/1") {
      status should equal (200)
      body should include ("Matt")
    }

    post("/api/1") {
      status should equal (200)
      body should include ("Matt")
    }

    post("/trademark") {
      status should equal (200)
      body should include ("")
    }
  }
}