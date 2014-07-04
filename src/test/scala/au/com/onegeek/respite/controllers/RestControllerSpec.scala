package au.com.onegeek.respite.controllers

import au.com.onegeek.respite.config.TestConfigurationModule
import reactivemongo.api.MongoDriver
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext
import au.com.onegeek.respite.api.ServletTestsBase
import au.com.onegeek.respite.controllers.RestController
import au.com.onegeek.respite.models.AccountComponents.User
import au.com.onegeek.respite.models.DefaultFormats._
import uk.gov.hmrc.mongo.{CurrentTime, ReactiveMongoFormats, ReactiveRepository, MongoConnector}
import au.com.onegeek.respite.models.ApiKey
import reactivemongo.bson.BSONObjectID
import reactivemongo.api.indexes.{IndexType, Index}
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import org.scalatest.concurrent.ScalaFutures

class RestControllerSpec extends ServletTestsBase with ScalaFutures with MongoEmbedDatabase  with MongoSpecSupport with Awaiting with CurrentTime {
  implicit val bindingModule = TestConfigurationModule

  var mongoProps: MongodProps = mongoStart(17123)
  val repository = new UserTestRepository

  addServlet(new RestController[User]("users", UserJsonFormat, repository), "/users/*")

  before {
    mongoProps = mongoStart() // by default port = 12345 & version = Version.2.3.0

    // Clear out entries - only do this if you don't start/stop between tests
    repository.removeAll

    // Add some keys to test against
    val key = User(id = Some(BSONObjectID("53b62e370100000100af8ecd")), username = "mfellows", firstName = "Matt")
    val key2 = User(id = Some(BSONObjectID("53b62e370100000100af8ece")), username = "bmurray", firstName = "Bill")
    await(repository.insert(key))
    await(repository.insert(key2))
  }

  after {
    mongoStop(mongoProps)
  }

  "A RestController" should {

    "Provide an API to fetch a all Models" in {
      get("/users/") {
        status should equal(200)
        println(body)
        body should equal("[{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"},{\"id\":{\"$oid\":\"53b62e370100000100af8ece\"},\"username\":\"bmurray\",\"firstName\":\"Bill\"}]")
      }
    }

    "Provide an API to fetch a single Model by it's ID" in {
      get("/users/53b62e370100000100af8ecd") {
        status should equal(200)
        body should equal("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
      }
    }

    "Provide an API to fetch a update a Model by it's ID" in {
      put("/users/1") {
        fail("not implemented")
      }
    }

    "Provide an API to create a Model" in {
      post("/users/") {
        status should equal(200)
        body should include("Matt")
      }
    }

    "Provide an API to delete a single Model by it's ID" in {
      delete("/users/1") {
        fail("not implemented")
      }
    }
  }
}