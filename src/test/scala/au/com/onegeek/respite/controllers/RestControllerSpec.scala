package au.com.onegeek.respite.controllers

import au.com.onegeek.respite.api.ServletTestsBase
import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.models.AccountComponents.User
import au.com.onegeek.respite.models.DefaultFormats._
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.CurrentTime

import scala.reflect._
import scala.reflect.runtime.universe._

class RestControllerSpec extends ServletTestsBase with ScalaFutures with MongoEmbedDatabase  with MongoSpecSupport with Awaiting with CurrentTime {
  implicit val bindingModule = TestConfigurationModule

  var mongoProps: MongodProps = mongoStart(17123)
  val repository = new UserTestRepository
  implicit val t = classTag[User]
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
      get("/users/", headers = Map("Accept" -> "application/json")) {
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

    "Provide an API to update a Model by it's ID" in {
      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}"
      put("/users/53b62e370100000100af8ecd", json.toString, Map("Content-Type" -> "application/json")) {
        status should equal(200)

      }

      val users = await(repository.findAll)
      users.foreach { u =>
        println(u)
      }

      val user = await(repository.findById(BSONObjectID("53b62e370100000100af8ecd")))
      user.get.firstName should equal("Harry")
    }


    "Send a 400 when an empty put body is sent" in {
      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}"
      put("/users/53b62e370100000100af8ecd", headers = Map("Content-Type" -> "application/json")) {
        status should equal(400)
      }
    }

    "Send a 400 when an invalid put body is sent" in {
      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"nousername\":\"mfellows\",\"firstName\":\"Harry\"}"
      put("/users/53b62e370100000100af8ecd", json.toString, headers = Map("Content-Type" -> "application/json")) {
        status should equal(400)
        body should equal ("{\"obj.username\":[{\"msg\":\"error.path.missing\",\"args\":[]}]}")
      }
    }

    "Send a 400 when incorrect headers sent with put???" in {
      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}"
      put("/users/53b62e370100000100af8ecd", json.toString) {
        status should equal(400)
        println(s"heres my body: ${body}")
      }
    }

    "Provide an API to create a Model" in {
      val json = "{\"username\":\"superman\",\"firstName\":\"Matt\"}"

      post("/users/", json.toString, Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should equal ("")
      }

      // Get response and then query
      val users = await(repository.findAll)
      users foreach(u =>
        u.username shouldNot equal("superman")
      )
    }

    "Send a 400 bad request on invalid JSON Model" in {
      val json = "{\"usernot\":\"mfellows\",\"firstName\":\"Matt\"}"

      post("/users/", json.toString, Map("Content-Type" -> "application/json")) {
        println(body)
        status should equal(400)
        body should equal ("{\"obj.username\":[{\"msg\":\"error.path.missing\",\"args\":[]}]}")
      }
    }

    "Provide an API to delete a single Model by it's ID" in {
      delete("/users/53b62e370100000100af8ecd") {
        status should equal(200)
        body should equal ("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
      }

      // Get response and then query
      val users = await(repository.findAll)
      users foreach(u =>
        u.username shouldNot equal("mfellows")
      )
    }
  }
}