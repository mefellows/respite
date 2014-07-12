package au.com.onegeek.respite.models

import au.com.onegeek.respite.config.TestConfigurationModule
import org.scalatest.concurrent.ScalaFutures
import au.com.onegeek.respite.models.AccountComponents.User
import reactivemongo.bson.BSONObjectID
import play.api.libs.json._
import au.com.onegeek.respite.UnitSpec


class ModelTests extends UnitSpec with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule

  "An Model Object" should {

    "Serialise to a sane JSON format" in {
      val user = new User(id = Some(BSONObjectID("53af77a90100000100a16ffb")), username = "mfellows", firstName = "Matt")
      println(Json.toJson(user))
      Json.toJson(user).toString should equal("{\"id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")

      val user2 = new User(username = "Hillary", firstName = "Hillman")
      println(Json.toJson(user2))

    }

    "Serialise from a sane JSON format" in {
      val user = new User(id = Some(BSONObjectID("53af77a90100000100a16ffb")), username = "mfellows", firstName = "Matt")
      val json = "{\"id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}"
      val user2: User = Json.parse(json).validate[User].get
      println(user2)
      user.id.get.stringify should equal(user2.id.get.stringify)
      user should equal (user2)
    }

    "Validate JSON objects without an id (for creating one)" in {
      val user = new User(username = "mfellows", firstName = "Matt")
      val json = Json.toJson(user)
      println(json)
    }
  }
}