package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.models.{Cat, User}
import au.com.onegeek.respite.test.{MongoSpecSupport, Awaiting}
import org.scalatest.concurrent.ScalaFutures
import org.scalatra.ScalatraServlet
import play.api.libs.json.{JsSuccess, _}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo._

import scala.reflect._
import au.com.onegeek.respite.controllers._
import au.com.onegeek.respite.config.TestConfigurationModule

/**
 * Created by mfellows on 29/06/2014.
 */
class MetricsSupportSpec extends ServletTestsBase with ScalaFutures with Awaiting with CurrentTime with MongoSpecSupport {
  implicit val bindingModule = TestConfigurationModule

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  val repository = new UserTestRepository

  addServlet(new MetricSpecController(repository = repository), "/users")
  addServlet(new MetricSpecControllerWithCustomName(repository = repository), "/users3/")
  addServlet(new RestController[User, BSONObjectID]("users", User.format, repository) with MetricsRestSupport[User, BSONObjectID], "/users2")
  addServlet(new MetricsController("/metrics"), "/metrics")

  before {
    // Clear out entries - only do this if you don't start/stop between tests
    await(repository.removeAll(reactivemongo.api.commands.WriteConcern.Unacknowledged))

    // Add some keys to test against
    val key = User(id = BSONObjectID("53b62e370100000100af8ecd"), username = "mfellows", firstName = "Matt")
    val key2 = User(id = BSONObjectID("53b62e370100000100af8ece"), username = "bmurray", firstName = "Bill")
    val cat = Cat(name = "Kitty", breed = "Shitzu")
    await(repository.insert(key))
    await(repository.insert(key2))

    println("Users in repo: ")
    val users = await(repository.findAll(reactivemongo.api.ReadPreference.primaryPreferred))
    users foreach(u =>
      println(u)
    )
  }

  "A MetricsSupport-ed RestController servlet" should {

    "Instrument a 'get' method (CRUD)" in {
      get("/users/") {
        status should equal(200)
        body should equal("[{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"},{\"id\":{\"$oid\":\"53b62e370100000100af8ece\"},\"username\":\"bmurray\",\"firstName\":\"Bill\"}]")
      }
      get("/metrics/") {
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.list\":{\"count\":1")
      }
      get("/metrics/health") {
        println(body)
        status should equal(200)
        body should include("\"RestController.Users.list\":{\"healthy\":true")
      }
    }

    "Instrument a 'get/:id' method (CRUD)" in {
      get("/users/53b62e370100000100af8ecd") {
        status should equal(200)
        body should equal("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
      }
      get("/metrics/") {
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.single\":{\"count\":1")
      }
    }

    "Instrument a 'delete' method (CRUD)" in {
      delete("/users/53b62e370100000100af8ecd") {
        status should equal(200)
        body should equal ("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
      }
      get("/metrics/") {
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.delete\":{\"count\":1")
      }

    }

    "Instrument a 'put' method (CRUD)" in {
      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}"
      put("/users/53b62e370100000100af8ecd", json, headers = Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should equal("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}")
      }
      get("/metrics/") {
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.update\":{\"count\":1")
      }
    }

    "Instrument a 'post' update method (CRUD)" in {
      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}"
      post("/users/53b62e370100000100af8ecd", json, headers = Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should equal("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}")
      }
      get("/metrics/") {
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.update\":{\"count\":2")
      }
    }

    "Instrument a 'post' create method (CRUD)" in {
      val json = "{\"username\":\"asuperman\",\"firstName\":\"Clarke\"}"
      post("/users/", json.toString, Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should include ("\"username\":\"asuperman\",\"firstName\":\"Clarke\"}")
      }
      get("/metrics/") {
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.create\":{\"count\":1")
      }
    }

    "Instrument a non-REST / random path, giving it a sensible name" in {
      get("/users/foo/bar/baz") {
        println(body)
        body should equal("foo")
      }
      get("/metrics/") {
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.get.foo_bar_baz\":{\"count\":1")
      }
    }

    "Instrument an ~ path" in {
      get("/users/~") {
        body should equal("~")
      }
      get("/metrics/") {
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.get.~\":{\"count\":1")
      }

    }
  }

  "A MetricsSupport-ed standard Scalatra Controller" should {

     "Instrument a non-declared RestController with sensible name" in {
      get("/users2/") {
        println(s"Body: $body")
        body should equal("[{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"},{\"id\":{\"$oid\":\"53b62e370100000100af8ece\"},\"username\":\"bmurray\",\"firstName\":\"Bill\"}]")
      }
      get("/metrics/") {
        println(body)
        body should include("\"RestController.Users.list\":{\"count\":1")
      }
      get("/metrics/health") {
        println(body)

        body should include("\"RestController.Users.list\":{\"healthy\":true")
      }
    }
  }

  "An unhealthy MetricsRestSupport-ed Controller" should {

    "Report unhealthy when a RestController is not responding with 200 response" in {

    }

    "Report unhealthy when Database connection is gone" in {
      mongoConnectorForTest.close()
      get("/users/") {
        status should equal(504)
      }

      // This happens early on in the event, why not regularly?
      get("/metrics/health") {
        body should include("\"RestController.Users.list\":{\"healthy\":false")
      }
    }
  }
}