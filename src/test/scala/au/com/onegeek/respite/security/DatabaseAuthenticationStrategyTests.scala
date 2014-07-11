package au.com.onegeek.respite.security

import au.com.onegeek.respite.config.TestConfigurationModule
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.scalatra.ScalatraServlet
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import org.scalatest.concurrent.ScalaFutures
import com.github.simplyscala.MongoEmbedDatabase
import uk.gov.hmrc.mongo._
import play.api.libs.json.{Json, JsValue}
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import com.github.simplyscala.MongodProps
import scala.Some
import scala.Tuple2
import reactivemongo.api.DefaultDB
import uk.gov.hmrc.mongo.MongoConnector
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.indexes.Index
import au.com.onegeek.respite.models.ApiKey
import au.com.onegeek.respite.ServletTestsBase

class ApiKeyTestRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[ApiKey, BSONObjectID]("testapikeys", mc.db, ApiKey.formats, ReactiveMongoFormats.objectIdFormats) {

  override def ensureIndexes() = {
    collection.indexesManager.ensure(Index(Seq("application" -> IndexType.Ascending), name = Some("applicationFieldUniqueIdx"), unique = true, sparse = true))
    collection.indexesManager.ensure(Index(Seq("key" -> IndexType.Ascending), name = Some("keyFieldUniqueIdx"), unique = true, sparse = true))
  }
}

class DatabaseAuthenticationStrategyTests extends ServletTestsBase with ScalaFutures with MongoEmbedDatabase with MongoSpecSupport with Awaiting {
  implicit val bindingModule = TestConfigurationModule

  class TestServlet(implicit val bindingModule: BindingModule) extends ScalatraServlet with Injectable

  var mongoProps: MongodProps = mongoStart(17124)
  val repository = new ApiKeyTestRepository
  val API_KEY_HEADER = "X-API-Key";
  val API_APP_HEADER = "X-API-Application";
  val validHeaders: Map[String, String] = Map(API_APP_HEADER -> "application-name", API_KEY_HEADER -> "key")

  class AuthServlet extends TestServlet with Authentication {
    override implicit val authenticationStrategy = new DatabaseAuthenticationStrategy(repository)

    get("/") {
      "OK"
    }

    override def initialize(config: ConfigT) {
      super.initialize(config)
    }
  }

  val authServlet = new AuthServlet
  val authServletWithApi = new AuthServlet with AuthenticationApi

  before {
    mongoProps = mongoStart() // by default port = 12345 & version = Version.2.3.0

    // Clear out entries
    repository.removeAll

    // Add some keys to test against
    val key = ApiKey(application = "application-name", description = "my description", key = "key")
    await(repository.insert(key))

  } // add your own port & version parameters in mongoStart method if you need it

  after {
    mongoStop(mongoProps)
  }

  addServlet(authServlet, "/*")
  addServlet(authServletWithApi, "/auth/*")

  "A DatabaseAuthenticationStrategy secured servlet" should {
    "reject requests without an API Key" in {
      get("/") {
        status should equal(401)
      }
    }

    "accept requests with a valid API Key" in {
      get("/", headers = validHeaders) {
        status should equal (200)
        body should equal ("OK")
      }
    }

    "Store stuff in repo" in {

      val e1 = ApiKey(application = "application-name1", description = "my description", key = "key1")
      val e2 = ApiKey(application = "application-name2", description = "my description", key = "key2")
      val e3 = ApiKey(application = "application-name3", description = "my description", key = "key3")
      val e4 = ApiKey(application = "application-name4", description = "my description", key = "key4")

      println(Json.toJson(e1).toString())

      val created = for {
        res1 <- repository.save(e1)
        res2 <- repository.save(e2)
        res3 <- repository.save(e3)
        countResult <- repository.count
      } yield countResult

      await(created) shouldBe 4

      val result: List[ApiKey] = await(repository.findAll)
      result.foreach (println)
      result.size shouldBe 4
      result should contain(e1)
      result should contain(e2)
      result should contain(e3)

      result should not contain (e4)
    }
  }

  "A Servlet with AuthenticationApi" should {

    "provide a RESTful API to remove keys at runtime" in {
      delete("/auth/token/key", headers = validHeaders) {
        status should equal (200)
        body should include ("Some(ApiKey(BSONObjectID")
      }

      // Key deleted, I should be rejected!
      get("/", headers = validHeaders) {
        status should equal (401)
      }
    }

    "reject a request with incorrect key" in {
      delete("/auth/token/notexist", headers = validHeaders) {
        status should equal (404)
      }
    }
  }
}