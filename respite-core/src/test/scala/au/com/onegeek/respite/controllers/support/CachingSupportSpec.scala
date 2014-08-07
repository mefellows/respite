package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.controllers._
import au.com.onegeek.respite.models.{Cat, User}
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo._
import org.scalamock.scalatest.MockFactory

/**
 * Created by mfellows on 29/06/2014.
 */
class CachingSupportSpec extends ServletTestsBase with ScalaFutures with Awaiting with CurrentTime with MongoSpecSupport {
  implicit val bindingModule = TestConfigurationModule

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  val repository = new UserTestRepository

  addServlet(new RestController[User, BSONObjectID]("users", User.format, repository) with CachingSupport, "/users")

  before {
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

  "A CachingSupport-ed RestController servlet" should {

    "with idempotent RESTful calls" should {

      "cache GET requests" in {

      }

      "expire GET requests on non-idempotent REST calls" in {

      }
    }
    "Caching (CRUD)" in {
      fail("not yet implemented")
//      get("/users/") {
//        status should equal(200)
//        body should equal("[{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"},{\"id\":{\"$oid\":\"53b62e370100000100af8ece\"},\"username\":\"bmurray\",\"firstName\":\"Bill\"}]")
//      }
//      get("/metrics/") {
//        body should include("\"au.com.onegeek.respite.controllers.MetricSpecController.list\":{\"count\":1")
//      }
//      get("/metrics/health") {
//        println(body)
//        status should equal(200)
//        body should include("\"RestController.Users.list\":{\"healthy\":true")
//      }
    }

    "" in {

    }
  }
}