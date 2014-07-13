package au.com.onegeek.respite.models

import play.api.libs.json._
import reactivemongo.bson._
import uk.gov.hmrc.mongo.ReactiveMongoFormats.objectIdFormats

//object AccountComponents {
//
//  case class SearchResults[T](
//                               elements: Seq[T],
//                               page: Int,
//                               pageSize: Int,
//                               total: Int
//                               )


/** Describes some common behavior of asset types */
case class User(id: BSONObjectID = BSONObjectID.generate, username: String, firstName: String) extends Model[BSONObjectID]

object User {

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  implicit val format = modelFormat {
    Json.format[User]
  }
}

case class Cat(id: BSONObjectID = BSONObjectID.generate, name: String, breed: String) extends Model[BSONObjectID]

object Cat {

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  implicit val format = modelFormat {
    Json.format[Cat]
  }
}