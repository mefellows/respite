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
package au.com.onegeek.respite.database

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

/**
 * Created by mfellows on 30/06/2014.
 */
object PlayJsonFormats {
  def read[T](json: JsValue)(implicit reader: Reads[T]): JsResult[T] = reader.reads(json)
  def write[T](obj: T)(implicit writer: Writes[T]): JsValue = writer.writes(obj)
}

trait PlayJsonFormats { self: PlayJsonFormats =>
  implicit val objectIdFormat = Format[BSONObjectID](
    (__ \ "$oid").read[String].map( obj => new BSONObjectID(obj) ),
    Writes[BSONObjectID]{ s => Json.obj( "$oid" -> s.stringify ) }
  )
}

trait DefaultFormats extends java.lang.Object with PlayJsonFormats {

}
object DefaultFormats extends java.lang.Object with DefaultFormats {

}