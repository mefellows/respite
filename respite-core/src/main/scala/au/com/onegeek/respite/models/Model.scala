/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Matt Fellows (OneGeek)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package au.com.onegeek.respite.models

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.JsonExtensions
import uk.gov.hmrc.mongo.ReactiveMongoFormats._

/**
 * A canonical representation of a Persisted Model.
 *
 * Created by mfellows on 30/06/2014.
 */
trait Model[ObjectID] {
  val id: ObjectID
}

/**
 * Model Extensions for Handling JSON and Mongo formats.
 */
object ModelJsonExtensions {
  import uk.gov.hmrc.mongo.ReactiveMongoFormats.objectIdFormats
  import uk.gov.hmrc.mongo.ReactiveMongoFormats.dateTimeFormats
  import uk.gov.hmrc.mongo.ReactiveMongoFormats.localDateTimeFormats

  implicit def StringToBSONObjectId(s: String): BSONObjectID = BSONObjectID(s)
  implicit def BSONObjectIdToString(s: BSONObjectID): String = s.stringify


  def withDefault[A](key: String, default: A)(implicit writes: Writes[A]) = __.json.update((__ \ key).json.copyFrom((__ \ key).json.pick orElse Reads.pure(Json.toJson(default))))

  /**
   * Use this Formatter for Mongo Repositories. It ensures the Id field of your Model object is always present,
   * and formatted correctly for ReactiveMongo database operations. Typically, you do not wish to use this
   * for in/out messaging via RestControllers etc.
   *
   * @param baseFormat Base formatter to wrap (typically, this is a `Json.format[Entity]`
   * @tparam A
   * @return
   */
  def modelFormatForMongo[A](baseFormat: Format[A]): Format[A] = {
    import uk.gov.hmrc.mongo.ReactiveMongoFormats._
    import JsonExtensions._

    val publicIdPath: JsPath = JsPath \ '_id
    val privateIdPath: JsPath = JsPath \ 'id

    new Format[A] {
      def reads(json: JsValue): JsResult[A] = baseFormat.compose(copyKey(publicIdPath, privateIdPath)).compose(withDefault("id", BSONObjectID.generate)).reads(json)
      def writes(o: A): JsValue = baseFormat.transform(moveKey(privateIdPath, publicIdPath)).writes(o)
    }
  }

  /**
   * Standard JSON formatter for your entity.  It ensures the Id field of your Model object is always present.
   * You would typically wrap this around a {{{Json.format[Entity]}}}.
   *
   * Known issue with this formatting. When an id field is sent with other invalid/missing fields, produces a weird responses like so:
   *
   * - {{{ {"obj.id.username":[{"msg":"error.path.missing","args":[]}],"obj.id.firstName":[{"msg":"error.path.missing","args":[]}]} }}}
   * - {{{ JsError(List((/id/username,List(ValidationError(error.path.missing,WrappedArray()))), (/id/firstName,List(ValidationError(error.path.missing,WrappedArray())))))}}}
   *
   * @param baseFormat
   * @tparam A
   * @return
   */
  def modelFormat[A](baseFormat: Format[A]): Format[A] = {
    import JsonExtensions._


    new Format[A] {
      def reads(json: JsValue): JsResult[A] = baseFormat.compose(withDefault("id", BSONObjectID.generate)).reads(json)
      def writes(o: A): JsValue = baseFormat.writes(o)
    }
  }
}