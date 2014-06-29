package au.com.respite.api.controllers.support

import au.com.respite.api.ServletTestsBase
import org.scalatest.concurrent.ScalaFutures
import au.com.respite.api.config.TestConfigurationModule
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import org.scalatra.{AsyncResult, ScalatraBase, ScalatraServlet}
import au.com.respite.api.security.Authentication
import reactivemongo.api.DefaultDB
import au.com.respite.api.models.AccountComponents.User
import au.com.respite.api.models.JsonFormats._
import scala.reflect._
import au.com.respite.api.models.{DefaultFormats, JsonFormats}
import au.com.respite.api.models.DefaultFormats._
import play.api.libs.json._
import scala.reflect.runtime.universe._
import au.com.respite.api.controllers.RestController
import au.com.respite.api.models.AccountComponents.User
import play.api.libs.json.JsSuccess

/**
 * Created by mfellows on 29/06/2014.
 */
class PlayJsonSupportSpec extends ServletTestsBase with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule

  class TestServlet extends ScalatraServlet

  val playServlet = new TestServlet with PlayJsonSupport[User] {
//  override implicit val formats: JsonFormats = DefaultFormats
     implicit val format: Format[User] = DefaultFormats.UserJsonFormat

    get("/") {
      JsSuccess(User(username="foo", firstName = "bar"))
    }

    post("/") {
      val postBody = request.get("myspecialkey")
      println(postBody)
      val u: JsResult[User] = parsedModel[User]

      u match {
        case model: JsSuccess[User] => {
            model.get.firstName
        }
        case e: JsError => {
          JsError.toFlatJson(e)
        }
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
      get("/") {
        status should equal(200)
        println(body)
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
        body should equal ("Matt")

        ()
      }
    }

    "Store a JsValue in the Request Map" in {
      post("/foo", "{\"_id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}", Map("Content-Type" -> "application/json")) {
        status should equal(200)
        ()
      }
    }
  }
}