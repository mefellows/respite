package au.com.respite.api

import org.scalatra.test.scalatest._
import reactivemongo.api.MongoDriver
import scala.util.Success
import scala.concurrent.ExecutionContext
import au.com.respite.api.controllers.UsersController
import au.com.respite.api.config.TestConfigurationModule

class RespiteAPIServletTests extends ScalatraSuite with org.scalatest.FunSuiteLike {
  protected implicit def executor: ExecutionContext = ExecutionContext.global
  implicit val bindingModule = TestConfigurationModule

  addServlet(new UsersController, "/api/*")

  test("The API contains the key user journey resources") {

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

  test("The API is pageable") {

  }

  test("The API is asynchronous") {

  }
}