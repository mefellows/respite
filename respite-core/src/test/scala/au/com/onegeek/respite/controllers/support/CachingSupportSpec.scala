package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.controllers._
import au.com.onegeek.respite.models.{ApiKey, Cat, User}
import au.com.onegeek.respite.security.Authentication
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import org.scalatest.concurrent.ScalaFutures
import org.scalatra.util.MultiMapHeadView
import org.scalatra.{RouteMatcher, FutureSupport, ScalatraServlet, Route}
import reactivemongo.bson.BSONObjectID
import spray.caching.LruCache
import uk.gov.hmrc.mongo._
import org.scalamock.scalatest.MockFactory
import org.scalatest.mock.MockitoSugar._

import scala.collection.immutable.HashMap
import scala.concurrent.{Future, Await, Promise, ExecutionContext}
import scala.concurrent.duration._
import scala.reflect.ClassTag
import org.joda.time.{Seconds, DateTimeZone, DateTime}
import org.joda.time.format.DateTimeFormat

/**
 * Created by mfellows on 29/06/2014.
 */
class CachingSupportSpec extends ServletTestsBase with ScalaFutures with Awaiting with CurrentTime with MongoSpecSupport {
  implicit val bindingModule = TestConfigurationModule

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  val repository = new UserTestRepository
  val m = mock[spray.caching.Cache[Any]]
  val myCache: spray.caching.Cache[Any] = LruCache()
  val BLOCKING_TIMEOUT = 100 millis

  class MockedCachingRouteSupport100Seconds extends ScalatraServlet with FutureSupport with CachingRouteSupport {
    protected implicit def executor: ExecutionContext = ExecutionContext.global
    override val timeToLive = 100 seconds
    override val timeToIdle = 10 seconds

    get("/") {
      "OK"
    }


    post("/notcacheable") {
      println("not cached")
    }
  }

  class MockedCachingRouteSupport366Days extends ScalatraServlet with FutureSupport with CachingRouteSupport {
    protected implicit def executor: ExecutionContext = ExecutionContext.global
    override val timeToLive = 366 days
    override val timeToIdle = 1 day

    get("/") {
      "OK"
    }


    post("/notcacheable") {
      println("not cached")
    }
  }

  class MockedCachingRouteSupport extends ScalatraServlet with FutureSupport with CachingRouteSupport {
    protected implicit def executor: ExecutionContext = ExecutionContext.global
    override lazy val cache = myCache

    get("/") {
      "OK"
    }

    put("/") {
      "Put"
    }

    patch("/") {
      "Patch"
    }

    post("/notcacheable") {
      "Post"
    }


    post("/") {
      "Post"
    }
    get("/redirect") {
      redirect(url("/"))
    }

    get("/explode") {
      status = 500
      throw new RuntimeException("shouldn't execute")
    }

//    get("/explode") {
//      throw new RuntimeException("shouldn't execute")
//    }

    delete("/:id") {
      "Delete"
    }

    options("/*") {
      "Options"
    }
  }

  addServlet(new MockedCachingRouteSupport, "/cache/*")
  addServlet(new MockedCachingRouteSupport100Seconds, "/cachesecs/*")
  addServlet(new MockedCachingRouteSupport366Days, "/cachedays/*")

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

  def assertCacheResult(key: String, result: Any, msg: String, code: Int = 200): Unit = {
      val res = Await.result(myCache.get(key).get, BLOCKING_TIMEOUT)
      myCache.get(key) should not be Nil
      res should equal(result)
  }

  "A CachingRouteSupport-ed RestController servlet" should {

    "with idempotent RESTful calls" should {

      "cache GET requests" in {
        myCache.size should equal (0)

        get("/cache") {
          myCache.size should equal (1)
          body should equal("OK")
          status should equal(200)
          assertCacheResult("GET", "OK", "Future didn't return 'OK' Response")
        }

        get("/cache") {
          body should equal("OK")
          status should equal(200)
          assertCacheResult("GET", "OK", "Future didn't return 'OK' Response")
        }

        // Inject Mock Caching Strategy into CachingRouteSupport and count entries

        // Check that Actual method was not invoked more than once!!
      }

      "Differentiate Cache entries based on k/v parameters in the request" in {
        myCache.size should equal (0)
        get("/cache/?foo=barman") {
          body should equal("OK")
          status should equal(200)
          assertCacheResult("GET/foo=barman", "OK", "Future didn't return 'OK' Response")
          myCache.size should equal (1)
        }
      }

      "Ensure requests with same query string k/v in different order is treated as single cache entry" in {
        // Or, does that really matter? Mmm, probably does
        fail("Not implemented yet")
      }

      "expire GET requests (cache entries) on non-idempotent REST calls (POST, PUT, DELETE) for matching URLs" in {
        myCache.size should equal(0)
        get("/cache") {
          body should equal("OK")
          status should equal(200)
          myCache.size should equal(1)
        }

        post("/cache") {
          myCache.size should equal(0)
        }

        get("/cache") {
          body should equal("OK")
          status should equal(200)
          myCache.size should equal(1)
        }

        // Check varient on common CRUD keys
        post("/cache/") {
          myCache.size should equal(0)
        }
      }
    }

    "When setting cache control headers" should {
      "Set \"Cache-Control:public\" when caching is enabled" in {
        get("/cache/") {
          header.get("Cache-Control").get.toLowerCase should equal("public")
        }
      }

      "Set accurate \"Expires\" header when caching is enabled" in {
        get("/cache/") {
          checkExpiryHeader(365 * 24 * 60)
        }

        get("/cachesecs/") {
          checkExpiryHeader(100 / 60)
        }

        get("/cachedays/") {
          checkExpiryHeader(365 * 24 * 60)
        }
      }

      "Not set \"Expires\" header for non-cacheable requests" in {
        post("/cachedays/notcacheable") {
          intercept[NoSuchElementException] {
            header.get("Cache-Control").get
          }
        }
      }

      def checkExpiryHeader(expiryInMinutes: Int, errorMargin: Int = 1): Unit = {
        // RFC2616 Spec: http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1
        val fmt = DateTimeFormat.forPattern("E, d MMM y kk:mm:ss z");
        val expiryHeader = header.get("Expires").get.replace("GMT", "UTC")
        val expectedExpiryDate = DateTime.now.plusMinutes(expiryInMinutes).withZone(DateTimeZone.UTC)
        val actualExpiry = fmt.withOffsetParsed.parseDateTime(expiryHeader)

        println(s"${fmt.print(expectedExpiryDate)}")
        println(s"${fmt.print(actualExpiry)}")

        // As this test and the controller test can be out by millis, the date expected can slip into the next second.
        // This is a way to ensure the date is within a second precision of accuracy
        val secs = Seconds.secondsBetween(actualExpiry.toInstant, expectedExpiryDate.toInstant).getSeconds

        if (secs > errorMargin) fail("Expiry date is out by at least 1 second: Expected \"" + fmt.print(expectedExpiryDate) + ", got \"" + fmt.print(actualExpiry) + "\"")
      }
    }

//    "When provided caching headers" should {
//      "" in {
//
//      }
//    }

    "with CRUD calls" should {

      "Cache HEAD requests " in {
        myCache.size should equal (0)
        head("/cache") {
          println(body)
          myCache.size should equal (1)
          status should equal(200)
          assertCacheResult("HEAD", "OK", "Expected 'OK'")
        }
      }

      "Cache OPTIONS requests " in {
        myCache.size should equal (0)
        options("/cache") {
          myCache.size should equal (1)
          status should equal(200)
          assertCacheResult("OPTIONS", "Options", "Expected 'Options'")
        }
      }

      "Not Cache DELETE requests" in {
        myCache.size should equal (0)
        delete("/cache/1") {
          myCache.size should equal (0)
          println(body)
          status should equal(200)
        }
      }

      "Not cache PUT requests" in {
        myCache.size should equal (0)
        put("/cache/") {
          myCache.size should equal (0)
          println(body)
          status should equal(200)
          body should equal("Put")
        }
      }

      "Not cache PATCH requests" in {
        myCache.size should equal (0)
        patch("/cache/") {
          myCache.size should equal (0)
          println(body)
          status should equal(200)
          body should equal("Patch")
        }
      }

      "Not cache POST requests" in {
        myCache.size should equal (0)
        post("/cache/notcacheable") {
          myCache.size should equal (0)
          println(body)
          status should equal(200)
          body should equal("Post")
        }
      }

      "NOT Cache 40x" in {
        myCache.size should equal (0)

        get("/nothereman") {
          myCache.size should equal (0)
          status should equal(404)
        }
      }

      "NOT Cache 30x" in {
        myCache.size should equal (0)
        get("/cache/redirect") {
          println(body)
          status should equal(302)
          myCache.size should equal (0)
          body should equal("")
        }
      }

      "NOT Cache 50x" in {
        myCache.size should equal (0)
        get("/cache/explode") {
          println(body)
          status should equal(500)
          myCache.size should equal (0)
          body should equal("")
        }
      }
    }

    "Provide an API to expire the cache" in {
      get("/cache") {
       assertCacheResult("GET", "OK", "Expected 'OK'")
        myCache.size should equal(1)
      }

      delete("/cache/cache/") {
        status should equal(200)
        myCache.size should equal(0)

        // Note: Entries aren't removed immediately it seems, but upon retrieval after 'clear'
      }
    }

    "Provide an API to expire individual cache entries" in {

      get("/cache") { }

      delete("/cache/cache/GET") {
        status should equal(200)
        myCache.size should equal(0)

        // Note: Entries aren't removed immediately it seems, but upon retrieval after 'clear'
      }

    }
  }

  "A CacheSupport-ed class" should {
    "when providing a caching DSL" should {

      "Cache arbitrary objects" in {

      }

      "map into a nice key" in {

      }
    }
  }
}
