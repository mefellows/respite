package au.com.onegeek.respite.models

import au.com.onegeek.respite.config.TestConfigurationModule
import org.scalatest.concurrent.ScalaFutures
import au.com.onegeek.respite.models.AccountComponents.User
import au.com.onegeek.respite.api.UnitSpec
import au.com.onegeek.respite.models.DefaultFormats._
import reactivemongo.bson.BSONObjectID
import play.api.libs.json._


class ModelTests extends UnitSpec with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule

  "An Model Object" should {

    "Serialise to a sane JSON format" in {
      val user = new User(id = Some(BSONObjectID("53af77a90100000100a16ffb")), username = "mfellows", firstName = "Matt")
      println(Json.toJson(user))
      Json.toJson(user).toString should equal("{\"_id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
    }


    "Validate JSON objects without an id (for creating one)" in {
      val user = new User(username = "mfellows", firstName = "Matt")
      val json = Json.toJson(user)
      println(json)
    }

    "reject requests with an invalid API Key" in {

    }
  }
}