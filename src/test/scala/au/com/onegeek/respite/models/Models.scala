package au.com.onegeek.respite.models

import reactivemongo.bson._
import play.api.libs.json._
import au.com.onegeek.respite.models.AccountComponents.User
import scala.Some
import reactivemongo.bson.BSONString
import uk.gov.hmrc.mongo.{ReactiveMongoFormats, TupleFormats}
import uk.gov.hmrc.mongo.ReactiveMongoFormats._
import reactivemongo.bson.BSONString
import scala.Some

object AccountComponents {

  case class SearchResults[T](
                               elements: Seq[T],
                               page: Int,
                               pageSize: Int,
                               total: Int
                               )

  /** Describes some common behavior of asset types */
  case class User(id: Option[BSONObjectID] = Some(BSONObjectID.generate), username: String, firstName: String) extends Model[BSONObjectID]

  object User {

    implicit val formats = {
      import uk.gov.hmrc.mongo.ReactiveMongoFormats._
      Json.format[User]
    }
  }

}

// Authentication
case class ApiKey(application: String, description: String, key: String)

object ApiKey {

  import ReactiveMongoFormats.mongoEntity

  implicit val formats = {
    import uk.gov.hmrc.mongo.ReactiveMongoFormats._
    Json.format[ApiKey]
  }
}

object BSONObjectIdFormats extends BSONObjectIdFormats

trait BSONObjectIdFormats {


}


object JsonFormats {

  def read[T](json: JsValue)(implicit reader: Reads[T]): JsResult[T] = reader.reads(json)

  def write[T](obj: T)(implicit writer: Writes[T]): JsValue = writer.writes(obj)
}

trait JsonFormats {
  self: JsonFormats =>

  //  implicit val objectIdFormat = Format[BSONObjectID](
  //    (__ \ "$oid").read[String].map( obj => new BSONObjectID(obj) ),
  //    Writes[BSONObjectID]{ s => Json.obj( "$oid" -> s.stringify ) }
  //  )

  import uk.gov.hmrc.mongo.ReactiveMongoFormats._

  // Generates Writes and Reads for Models thanks to json Macros
  implicit val ApiKeysJsonFormat = Json.format[ApiKey]
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