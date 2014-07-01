package au.com.onegeek.respite.models

import au.com.onegeek.respite.config.TestConfigurationModule
import org.scalatest.concurrent.ScalaFutures
import au.com.onegeek.respite.models.User
import reactivemongo.bson.BSONObjectID
import play.api.libs.json._
import au.com.onegeek.respite.UnitSpec


class ModelTests extends UnitSpec with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule

  "An Model Object" should {

    "Serialise to a sane JSON format" in {
      val user = new User(id = BSONObjectID("53af77a90100000100a16ffb"), username = "mfellows", firstName = "Matt")
      println(Json.toJson(user))
      Json.toJson(user).toString should equal("{\"id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}")
      Json.toJson(user).toString should include ("53af77a90100000100a16ffb")

      val user2 = new User(username = "Hillary", firstName = "Hillman")
      println(Json.toJson(user2))

    }

    "Serialise from a sane JSON format" in {
      val user = new User(id = BSONObjectID("53af77a90100000100a16ffb"), username = "mfellows", firstName = "Matt")
      val json = "{\"id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}"
      val user2: User = Json.parse(json).validate[User].get
      println(user2)
      user.id.stringify should equal(user2.id.stringify)
      user should equal (user2)
    }

    "Validate JSON objects without an id (for creating one)" in {
      val user = new User(username = "mfellows", firstName = "Matt")
      val json = Json.toJson(user).toString()


      val jsonToUser = "{\"username\":\"mfellows\",\"firstName\":\"Matt\"}"

      val parsedUser = Json.parse(jsonToUser).validate[User].getOrElse( {println("Fail!"); fail("Validation failed for model without an ID")})
      parsedUser.firstName should equal ("Matt")
      println(parsedUser.id)

      Json.parse(jsonToUser).validate[User] match {
        case e: JsError => println(s"${e}"); fail("Validation Error")
        case e: JsSuccess[User] => e.get.firstName should equal("Matt")
      }
    }

    "Provide sensible validation error messages" in {
//      val json = "{\"firstName\":\"Matt\"}"
//      val json = "{\"_id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"firstName\":\"Matt\", \"username\":\"foo\"}"
      val json = "{\"firstName\":\"Matt\", \"nousername\":\"foo\"}"
      Json.parse(json).validate[User] match {
        case e: JsError => println(s"${JsError.toFlatJson(e)}"); JsError.toFlatJson(e).toString should equal ("{\"obj.username\":[{\"msg\":\"error.path.missing\",\"args\":[]}]}")
        case e: JsSuccess[User] => e.get.firstName should equal("Matt")
      }
    }

    "Provide sensible validation error messages - ID sent but other fields not" in {
//      val json = "{\"nofirstName\":\"Matt\", \"nousername\":\"foo\",\"id\":{\"$oid\":\"53af77a90100000100a16ffb\"}}"
      val json = "{\"firstName\":\"Matt\", \"nousername\":\"foo\"}"
      Json.parse(json).validate[User] match {
        case e: JsError =>
          println(s"${JsError.toFlatJson(e)}")

          // TODO: It should probably equal this:
          println(e)
          JsError.toFlatJson(e).toString should equal ("{\"obj.username\":[{\"msg\":\"error.path.missing\",\"args\":[]}]}")
          // But it actually equals this {"obj.id.username":[{"msg":"error.path.missing","args":[]}]}
          // Note the id field sneaking it's way in there.
      }
    }
  }
}