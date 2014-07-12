package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.models.User
import au.com.onegeek.respite.test.Awaiting
import org.scalatest.concurrent.ScalaFutures
import org.scalatra.ScalatraServlet
import play.api.libs.json.{JsSuccess, _}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo._

import scala.reflect._

/**
 * Created by mfellows on 29/06/2014.
 */
class PlayJsonSupportSpec extends ServletTestsBase with ScalaFutures with Awaiting with CurrentTime {
  implicit val bindingModule = TestConfigurationModule

  implicit val tag = classTag[User]
  class TestServlet(implicit val tag: ClassTag[User]) extends ScalatraServlet

  val playServlet = new TestServlet with PlayJsonSupport[User] { //this: PlayJsonSupport =>
    override implicit val format: Format[User] = User.format

    get("/") {
      JsSuccess(User(id = BSONObjectID("53aeb92ab65f2a89219ddcfb"), username="foo", firstName = "bar"))
    }

    get("/some") {
      Some(User(id = BSONObjectID("53aeb92ab65f2a89219ddcfb"), username="foo", firstName = "bar"))
    }

    post("/fail") {
    }

    get("/none") {
      None
    }

    get("/list") {
      List(User(id = BSONObjectID("53aeb92ab65f2a89219ddcfb"), username="foo", firstName = "bar"))
    }

    get("/amodel") {
      User(id = BSONObjectID("53aeb92ab65f2a89219ddcfb"), username="foo", firstName = "bar")
    }

    get("/plain") {
      contentType = formats("html")
      User(id = BSONObjectID("53aeb92ab65f2a89219ddcfb"), username="foo", firstName = "bar")
    }

    post("/") {
      parsedModel[User] match {
        case model: JsSuccess[User] =>
          println(model.get.firstName)
          assert(model.get.firstName == "Matt")
//          assert(model.get.firstName == "Matt" && model.get.username == "mfellows" && model.get._id.get == BSONObjectID("53aeb92ab65f2a89219ddcfb"))
          model.get
        case e: JsError => fail(s"Should not be an error. Error ${e.errors}")
      }
    }

    post("/foo") {
      val postBody = request.get("myspecialkey")
      getParsedModel
    }

    override def initialize(config: ConfigT) {
      super.initialize(config)
    }
  }

  addServlet(playServlet, "/*")

  "A JSON support servlet" should {

    "Transparently convert Models into JSON" in {
      get("/amodel") {
        status should equal(200)
        body should equal("{\"id\":{\"$oid\":\"53aeb92ab65f2a89219ddcfb\"},\"username\":\"foo\",\"firstName\":\"bar\"}")
      }
    }

    "Transparently convert JsResult (JsSuccess) objects into JSON" in {
      get("/") {
        status should equal(200)
        println(body)
      }
    }

    "Transparently convert JsFailure (JsError) objects into JSON" in {
      val json = "{\"username\":\"foo\",\"ffirstName\":\"bar\",\"_id\":{\"$oid\":\"53aeb92ab65f2a89219ddcfb\"}}"

      post("/fail", body = json, headers = Map("Content-Type" -> "application/json")) {
        body should equal("{\"obj.firstName\":[{\"msg\":\"error.path.missing\",\"args\":[]}]}")
        status should equal(400)
      }
    }

    "Convert Lists of Models into JSON" in {
      get("/list") {
        status should equal(200)
        println(body)
      }
    }

    "Convert an Option of a Model into JSON" in {
      get("/some") {
        status should equal(200)
        println(body)
      }
    }

    "Return a 404 when a None (Option[Model]) is returned" in {
      get("/none") {
        status should equal(404)
      }
    }

    "Stay out of the way for non-JSON requests" in {
      get("/plain", headers = Map("Accept" -> "text/html")) {
       println(body)
        status should equal(200)
        body should equal("User(BSONObjectID(\"53aeb92ab65f2a89219ddcfb\"),foo,bar)")
      }
    }

    "Store the validated Model object in the Request map" in {
      post("/", "{\"id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}", Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should equal("{\"id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
      }
    }
  }
}