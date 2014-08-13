package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.controllers._
import au.com.onegeek.respite.models.{ApiKey, Cat, User}
import au.com.onegeek.respite.security.Authentication
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import org.scalatest.concurrent.ScalaFutures
import org.scalatra.{FutureSupport, ScalatraServlet, Route}
import reactivemongo.bson.BSONObjectID
import spray.caching.LruCache
import uk.gov.hmrc.mongo._
import org.scalamock.scalatest.MockFactory
import org.scalatest.mock.MockitoSugar._

import scala.concurrent.{Future, Await, Promise, ExecutionContext}
import scala.concurrent.duration._
import scala.reflect.ClassTag

/**
 * Created by mfellows on 29/06/2014.
 */
class CachingSupportSpec extends ServletTestsBase with ScalaFutures with Awaiting with CurrentTime with MongoSpecSupport {
  implicit val bindingModule = TestConfigurationModule

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  val repository = new UserTestRepository
  val m = mock[spray.caching.Cache[Any]]
  val myCache: spray.caching.Cache[Any] = LruCache()

  class MockedCachingRouteSupport extends ScalatraServlet with FutureSupport with CachingRouteSupport {
    protected implicit def executor: ExecutionContext = ExecutionContext.global
//    override implicit val listCache = m
    override val listCache: spray.caching.Cache[Any] = myCache

    get("/") {
      "OK"
    }

    post("/notcacheable") {
      println("not cached")
    }

  }

  addServlet(new MockedCachingRouteSupport, "/")

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

    myCache.clear()
    println(s"My cache size: ${myCache.size}")
  }

  "A CachingRouteSupport-ed RestController servlet" should {

    "with idempotent RESTful calls" should {

      "cache GET requests" in {
        myCache.size should equal (0)

        get("/") {
          myCache.size should equal (1)
          body should equal("OK")
          status should equal(200)

          myCache.get("GET").flatMap { f =>
            val r = f.map({ b =>
              b should equal("OK")
              Some(b)
            })
            Some(r)
          }.getOrElse( {
            fail("Future didn't return 'OK' Response")
          })
        }

        get("/") {
          body should equal("OK")
          status should equal(200)
          myCache.get("GET") should not be Nil

          myCache.get("GET").flatMap { f =>
            val r = f.map({ b =>
              b should equal("OK")
              Some(b)
            })
            Some(r)
          }.getOrElse( {
            fail("Future didn't return 'OK' Response")
          })

        }
        // Inject Mock Caching Strategy into CachingRouteSupport and count entries


        // Assert empty cache, and request /users/

        // Assert cache has 1 item in it for 'list'
      }

      "expire GET requests (cache entries) on non-idempotent REST calls (POST, PUT, DELETE)" in {

        get("/") {
          myCache.get("GET").flatMap { f =>
            val r = f.map({ b =>
              b should equal("OK")
              Some(b)
            })
            Some(r)
          }.getOrElse( {
            fail("Future didn't return 'OK' Response")
          })

        }

        delete("/cache/expire") {
          status should equal(200)
          myCache.size should equal(0)

          // Note: Entries aren't removed immediately it seems, but upon retrieval after 'clear'
        }
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
        val size = myCache.size
        post("/notcacheable") {
          myCache.size should equal(size)
        }
      }

      "Cache 40x" in {

      }

      "Cache 30x" in {

      }

      "Cache 50x" in {

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

  "A Cache" should {
    "Never consume more memory than allowed" in {

    }

    "Store high-use objects in memory and fall back to 2nd-level cache?" in {

    }
  }
}