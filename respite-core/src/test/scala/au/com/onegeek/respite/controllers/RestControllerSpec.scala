package au.com.onegeek.respite.controllers

import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.models._
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.CurrentTime
import au.com.onegeek.respite.controllers.support.MetricsSupport

class RestControllerSpec extends ServletTestsBase with ScalaFutures with MongoSpecSupport with Awaiting with CurrentTime {
  implicit val bindingModule = TestConfigurationModule
//  var mongoProps: MongodProps = null

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  val repository = new UserTestRepository
  val catRepository = new CatTestRepository

  addServlet(new CatController(repository = catRepository), "/cats/*")
  addServlet(new RestController[User, BSONObjectID]("users", User.format, repository), "/users/*")

  before {
    //mongoProps = mongoStart(17123) // by default port = 12345 & version = Version.2.3.0

    // Clear out entries - only do this if you don't start/stop between tests
    await(repository.removeAll)
    await(catRepository.removeAll)

    // Add some keys to test against
    val key = User(id = BSONObjectID("53b62e370100000100af8ecd"), username = "mfellows", firstName = "Matt")
    val key2 = User(id = BSONObjectID("53b62e370100000100af8ece"), username = "bmurray", firstName = "Bill")
    val cat = Cat(name = "Kitty", breed = "Shitzu")
    await(repository.insert(key))
    await(repository.insert(key2))
    await(catRepository.insert(cat))

    println("Users in repo: ")
    val users = await(repository.findAll)
    users foreach(u =>
      println(u)
    )

    println("Cats in repo: ")
    val cats = await(catRepository.findAll)
    cats foreach(u =>
      println(u)
    )
  }

  after {
    //mongoStop(mongoProps)
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
      get("/users/53b62e370100000100af8ecd", headers = Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should equal("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
      }
    }

    "Provide an API to update a Model by it's ID" in {
      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}"
      put("/users/53b62e370100000100af8ecd", json, headers = Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should equal("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}")
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
//      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"nousername\":\"mfellows\",\"irstName\":\"Harry\"}"
      val json = "{\"nousername\":\"mfellows\",\"nofirstName\":\"Harry\"}"
      put("/users/53b62e370100000100af8ecd", json.toString, headers = Map("Content-Type" -> "application/json")) {
        println(body)
        status should equal(400)
        body should equal ("{\"obj.username\":[{\"msg\":\"error.path.missing\",\"args\":[]}],\"obj.firstName\":[{\"msg\":\"error.path.missing\",\"args\":[]}]}")
      }
    }

    "Send a 400 when incorrect headers sent with put???" in {
      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}"
      put("/users/53b62e370100000100af8ecd", json) {
        status should equal(400)
        println(s"heres my body: ${body}")
      }
    }


    // OK, so we have a problem - it doesn't seem to save if ID not provided. hmmm....
    "Provide an API to create a Model" in {
//      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8eca\"},\"username\":\"aoeu\",\"firstName\":\"aoeu\"}"
      val json = "{\"username\":\"superman\",\"firstName\":\"Clarke\"}"
      post("/users/", json.toString, Map("Content-Type" -> "application/json")) {
        println(s"heres my body: ${body}")
        status should equal(200)
        body should include ("\"username\":\"superman\",\"firstName\":\"Clarke\"}")
      }

      // Get response and then query
      await(repository.findAll).find(_.username.equals("superman")).size should equal (1)
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

    "Respond with 500 Internal Server error when Database is unavailable" in {

    }

    "Respond with 500 Internal Server error when Akka cluster is unavailable" in {

    }

  }
}