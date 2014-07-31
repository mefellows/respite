package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.models.{Cat, User}
import au.com.onegeek.respite.test.{MongoSpecSupport, Awaiting}
import org.scalatest.concurrent.ScalaFutures
import org.scalatra.ScalatraServlet
import play.api.libs.json.{JsSuccess, _}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo._

import scala.reflect._
import au.com.onegeek.respite.controllers._

/**
 * Created by mfellows on 29/06/2014.
 */
class MetricsSupportSpec extends ServletTestsBase with ScalaFutures with Awaiting with CurrentTime with MongoSpecSupport {
  implicit val bindingModule = TestConfigurationModule

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  val repository = new UserTestRepository

  addServlet(new MetricSpecController(repository = repository), "/users")
  addServlet(new RestController[User, BSONObjectID]("users", User.format, repository) with MetricsRestSupport[User, BSONObjectID], "/users2")
  addServlet(new MetricsController("/metrics"), "/metrics")

  before {
    //mongoProps = mongoStart(17123) // by default port = 12345 & version = Version.2.3.0

    // Clear out entries - only do this if you don't start/stop between tests
    await(repository.removeAll)

    // Add some keys to test against
    val key = User(id = BSONObjectID("53b62e370100000100af8ecd"), username = "mfellows", firstName = "Matt")
    val key2 = User(id = BSONObjectID("53b62e370100000100af8ece"), username = "bmurray", firstName = "Bill")
    val cat = Cat(name = "Kitty", breed = "Shitzu")
    await(repository.insert(key))
    await(repository.insert(key2))

    println("Users in repo: ")
    val users = await(repository.findAll)
    users foreach(u =>
      println(u)
      )
  }

  "A MetricsSupport-ed servlet" should {

    "Transparently instrument a 'get' method (CRUD)" in {
      get("/users/") {
        println(body)

        // Body should not be impacted by metrics, check that
      }
      get("/metrics/") {
        println(body)
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.get.list\":{\"count\":1")
      }
    }

    "Transparently instrument a 'get/:id' method (CRUD)" in {
      get("/users/53b62e370100000100af8ecd") {
        println(body)
      }
      get("/metrics/") {
        println(body)

        // Body should not be impacted by metrics, check that
        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.get.single\":{\"count\":1")
      }
    }

    "Transparently instrument a random path, giving it a sensible name" in {
      get("/users/fooaoeuaoeu/bar/baz") {
        println(body)
        // Body should not be impacted by metrics, check that
      }
      get("/metrics/") {
        println(body)

        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.get.fooaoeuaoeubarbaz\":{\"count\":1")
      }
    }

    "properly instrument a non-declared RestController with sensible name" in {
      get("/users2/") {
        println(body)
        // Body should not be impacted by metrics, check that
      }
      get("/metrics/") {
        println(body)

        body should include("\"RestController.Users.get.list\":{\"count\":1")
      }
      get("/metrics/health") {
        println(body)

//        body should include("\"RestController.Users.get.list\":{\"count\":1")
      }
    }
  }
}