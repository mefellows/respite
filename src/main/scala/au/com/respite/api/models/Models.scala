package au.com.respite.api.models

import reactivemongo.bson._
import play.api.libs.json._
import play.api.libs.json.Reads._
import au.com.respite.api.models.AccountComponents.User
import scala.Some
import reactivemongo.bson.BSONString
import au.com.respite.api.models.AccountComponents.Foo

object AccountComponents {

  case class SearchResults[T](
                               elements: Seq[T],
                               page: Int,
                               pageSize: Int,
                               total: Int
                               )

  /** Describes some common behavior of asset types */
  trait Model {
  }

  case class User(_id: Option[BSONObjectID] = Some(BSONObjectID.generate), username: String, firstName: String) extends Model
  case class Foo(_id: Option[BSONObjectID]) extends  Model

}

// Authentication
case class ApiKey(application: String, description: String, key: String)

object BSONObjectIdFormats extends BSONObjectIdFormats

trait BSONObjectIdFormats {


}


object JsonFormats {

  def read[T](json: JsValue)(implicit reader: Reads[T]): JsResult[T] = reader.reads(json)
  def write[T](obj: T)(implicit writer: Writes[T]): JsValue = writer.writes(obj)
}

trait JsonFormats { self: JsonFormats =>

  //  implicit val objectIdRead: Reads[BSONObjectID] = __.read[String].map {
  //    oid => BSONObjectID(oid)
  //  }
  //
  //  implicit val objectIdWrite: Writes[BSONObjectID] = new Writes[BSONObjectID] {
  //    def writes(oid: BSONObjectID): JsValue = JsString(oid.stringify)
  //  }
  //
  //  implicit val objectIdFormats = Format(JsonFormats.objectIdRead, JsonFormats.objectIdWrite)

  implicit val objectIdFormat = Format[BSONObjectID](
    (__ \ "$oid").read[String].map( obj => new BSONObjectID(obj) ),
    Writes[BSONObjectID]{ s => Json.obj( "$oid" -> s.stringify ) }
  )

  // Generates Writes and Reads for Models thanks to json Macros
  //  implicit val modelFmt = Json.format[Model]
  implicit val ApiKeysJsonFormat = Json.format[ApiKey]
  implicit val FooJsonFormat = Json.format[Foo]
  implicit val UserJsonFormat = Json.format[User]
}

trait DefaultFormats extends java.lang.Object with JsonFormats {

}
object DefaultFormats extends java.lang.Object with DefaultFormats {

}

object DAOMappers {

  import AccountComponents._

  implicit def BSONObjectIdToBSONString(b: BSONObjectID): BSONString = BSONString(b.stringify)

  implicit def BSONStringToBSONObjectId(s: BSONString): BSONObjectID = BSONObjectID(s.value)

  // How to do this and format the _id as a sane 'id' field.
  implicit val ApiKeysFormat = Macros.handler[ApiKey]
  implicit val UserFormat = Macros.handler[User]
}