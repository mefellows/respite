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

  addServlet(new RestController[User, BSONObjectID]("users", User.format, repository) with CachingRouteSupport, "/users")

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

  "A CachingRouteSupport-ed RestController servlet" should {


    markup { """

Mutable Set
-----------

A set is a collection that contains no duplicate elements.

To implement a concrete mutable set, you need to provide implementations
of the following methods:

    def contains(elem: A): Boolean
    def iterator: Iterator[A]
    def += (elem: A): this.type
    def -= (elem: A): this.type

If you wish that methods like `take`,
`drop`, `filter` return the same kind of set,
you should also override:

    def empty: This

It is also good idea to override methods `foreach` and
`size` for efficiency.

             """ }

    "with idempotent RESTful calls" should {

      "cache GET requests" in {
        // Inject Caching Strategy into CachingRouteSupport

        // Assert empty cache, and request /users/

        // Assert cache has 1 item in it for 'list'
      }

      "expire GET requests (cache entries) on non-idempotent REST calls (POST, PUT, DELETE)" in {

      }
    }

    "with CRUD calls" should {

      "Cache HEAD requests " in {

      }

      "Cache OPTIONS requests " in {

      }

      "Cache GET requests " in {

      }

      "Cache DELETE requests" in {

      }

      "Not cache PUT requests" in {

      }

      "Not cache POST requests" in {

      }

//      fail("not yet implemented")
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

    "Provide an API to expire the cache" in {

    }

    "Provide an API to expire individual cache entries" in {

    }
  }

  "A CacheSupport-ed class" should {
    "when providing a caching DSL" should {

      "Cache arbitrary objects" in {

      }

      "" in {

      }

    }
  }
}