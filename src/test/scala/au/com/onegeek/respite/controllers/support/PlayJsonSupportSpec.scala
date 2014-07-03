package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.api.ServletTestsBase
import org.scalatest.concurrent.ScalaFutures
import au.com.onegeek.respite.config.TestConfigurationModule
import org.scalatra.{ApiFormats, ScalatraServlet}
import au.com.onegeek.respite.models.DefaultFormats
import play.api.libs.json._
import play.api.libs.json.JsSuccess
import au.com.onegeek.respite.models.AccountComponents.User
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo._
import au.com.onegeek.respite.test.Awaiting


/**
 * Created by mfellows on 29/06/2014.
 */
class PlayJsonSupportSpec extends ServletTestsBase with ScalaFutures with Awaiting with CurrentTime {
  implicit val bindingModule = TestConfigurationModule

  class TestServlet extends ScalatraServlet

  val playServlet = new TestServlet with PlayJsonSupport[User] {
    implicit val format = User.formats

    get("/") {
      JsSuccess(User(_id = Some(BSONObjectID("53aeb92ab65f2a89219ddcfb")), username="foo", firstName = "bar"))
    }

    get("/amodel") {
      User(_id = Some(BSONObjectID("53aeb92ab65f2a89219ddcfb")), username="foo", firstName = "bar")
    }

    post("/") {
      val obj = parsedModel[User]
      obj match {
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
        body should equal("{\"_id\":{\"$oid\":\"53aeb92ab65f2a89219ddcfb\"},\"username\":\"foo\",\"firstName\":\"bar\"}")
      }
    }

    "Transparently convert JsResult (JsSuccess) objects into JSON" in {
      get("/") {
        status should equal(200)
        println(body)
      }
    }

    "Transparently convert JsFailure (JsError) objects into JSON" in {
      get("/") {
        fail("Not yet implemented")
      }
    }

    "Convert Lists of Models into JSON" in {

    }

    "Stay out of the way for non-JSON requests" in {

    }

    "Fail invalid JSON requests" in {

    }

    "Store the validated Model object in the Request map" in {
      post("/", "{\"_id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}", Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should equal("{\"_id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
      }
    }
  }
}