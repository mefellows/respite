package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.models.{Cat, User}
import au.com.onegeek.respite.test.{MongoSpecSupport, Awaiting}
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatra.ScalatraServlet
import play.api.libs.json.{JsSuccess, _}
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.Future
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

    "Update a mongo object" in {
//      val json = "{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}"
//      post("/users/53b62e370100000100af8ecd", json, headers = Map("Content-Type" -> "application/json")) {
//        status should equal(200)
//        body should equal("{\"id\":{\"$oid\":\"53b62e370100000100af8ecd\"},\"username\":\"mfellows\",\"firstName\":\"Harry\"}")
//      }

//      val u = User(username = "mfellows2", firstName = "Harry")
//
//      val foo = await(insert(u))
//      println(s"inserted something: ${foo}")
//
//      val foo2 = await(find(u))
//      println(s"found something: ${foo2}")

      val u2 = User(id = BSONObjectID("53b62e370100000100af8ecd"), firstName = "baggins", username = "mfellows")
      val foo3 = await(update(u2, BSONObjectID("53b62e370100000100af8ecd")))
      println(s"updated something: ${foo3}")

      val foo4 = await(findAll)
      println(s"found something: ${foo4}")


    }

    def update(u: User, id: BSONObjectID) = {
      import play.modules.reactivemongo.json._
      User.format.writes(u) match {
        case d @ JsObject(_) =>

//          val command = FindAndModify(repository.collection.name,
//            BSONDocument("_id" -> id),
//            Update(
//              BSONDocument("$set" -> d),
//              fetchNewObject = true
//            ),
//            upsert = false
//          )
//
//          repository.collection.db.command(command)

          repository.collection.findAndUpdate(BSONDocument("_id" -> id), BSONDocument("$set" -> d), fetchNewObject = true)
        case _ =>
          Future.failed[WriteResult](new Exception("cannot write object"))
      }
    }

    def find(u: User) = {
      import play.modules.reactivemongo.json._
      User.format.writes(u) match {
        case d @ JsObject(_) => repository.collection.find(d).cursor[User].collect[List]()
        case _ =>
          Future.failed[WriteResult](new Exception("cannot write object"))
      }
    }

    def findAll = {
      import play.modules.reactivemongo.json._
      repository.collection.find(Json.obj()).cursor[User].collect[List]()
    }

    def insert(u: User) = {
      import play.modules.reactivemongo.json._
      User.format.writes(u) match {
        case d @ JsObject(_) => repository.collection.insert(d)
        case _ =>
          Future.failed[WriteResult](new Exception("cannot write object"))
      }
    }

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