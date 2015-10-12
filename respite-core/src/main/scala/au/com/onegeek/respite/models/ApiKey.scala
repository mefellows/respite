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

import java.security.MessageDigest

import uk.gov.hmrc.mongo.json.JsonExtensions._
import uk.gov.hmrc.mongo.json.{JsonExtensions, ReactiveMongoFormats}
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import au.com.onegeek.respite.models.ModelJsonExtensions._

/**
 * Created by mfellows on 4/07/2014.
 */

case class ApiKey(id: BSONObjectID = BSONObjectID.generate, application: String, description: String, key: String) extends Model[BSONObjectID]

object ApiKey {

  import au.com.onegeek.respite.models.ModelJsonExtensions._
  import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

  implicit val format = //modelFormatForApiKey {
    modelFormat {
      Json.format[ApiKey]
    }
//  }

  def generateKey(application: String, description: String): String = {
    MessageDigest.getInstance("MD5").digest(application.+(description).getBytes()).map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }
//
//  /**
//   * Use this Formatter for Mongo Repositories. It ensures the Id field of your Model object is always present,
//   * and formatted correctly for ReactiveMongo database operations. Typically, you do not wish to use this
//   * for in/out messaging via RestControllers etc.
//   *
//   * @param baseFormat Base formatter to wrap (typically, this is a `Json.format[Entity]`
//   * @tparam A
//   * @return
//   */
//  def modelFormatForApiKey[A](baseFormat: Format[A]): Format[A] = {
//    import uk.gov.hmrc.mongo.json.ReactiveMongoFormats._
//    import JsonExtensions._
//
//    new Format[A] {
//      def reads(json: JsValue): JsResult[A] = {
//        val hash: String = generateKey( (json \ "application").as[String], ((json \ "description").as[String]) )
//        baseFormat.compose(withDefault("key", hash)).reads(json)
//      }
//      def writes(o: A): JsValue = baseFormat.writes(o)
//    }
//  }
}