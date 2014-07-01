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
package au.com.onegeek.respite.controllers.support

import org.scalatra._
import scala.annotation.implicitNotFound
import play.api.libs.json._
import scala.io.Codec
import javax.servlet.http.HttpServletRequest
import org.scalatra.MatchedRoute
import play.api.libs.json.JsSuccess
import scala.Some
import reactivemongo.bson.BSONObjectID

/**
 * Created by mfellows on 27/06/2014.
 */
object PlayJsonSupport {
  val ParsedModelKey = "au.com.onegeek.respite.parsedModel"
}

@implicitNotFound(
  "No play JSON Reads/Writes defaults are in place. Try adding au.com.onegeek.respite.database.DefaultFormats or creating your own."
)
trait PlayJsonSupport[T] extends ScalatraBase {

  import PlayJsonSupport._

  // Hoping to be able to go back to this to avoid passing Type Parameters around in all subclasses. OK for now.
  //  implicit val formats: JsonFormats

  // Put this back
  implicit val format: Format[T]

  private def shouldParseBody(fmt: String)(implicit request: HttpServletRequest) = (fmt == "json")


  override protected def invoke(matchedRoute: MatchedRoute) = {
    withRouteMultiParams(Some(matchedRoute)) {

      val mt = request.contentType map {
        _.split(";").head
      } getOrElse "application/x-www-form-urlencoded"

      // Extract  the JSON request body if the message is a JSON object
      if (shouldParseBody(mt)) {
        //      request(ParsedBodyKey) = parseRequestBody(fmt).asInstanceOf[AnyRef]
        //      Option(request.body) filterNot {_.isEmpty} map {s => request(ParsedModelKey) = Some(Json.parse(request.body).validate[T])}
        Option(request.body) filterNot {
          _.isEmpty
        } map {
          s => request(ParsedModelKey) = Some(Json.parse(request.body).validate[T])
        }
      }


      super.invoke(matchedRoute)
    }
  }

  override protected def renderPipeline = ({
    // Note the following from the JValueResult class -> Secret sauce is PartialFunctions?

    //    case a: Any if isJValueResponse && customSerializer.isDefinedAt(a) =>
    //      customSerializer.lift(a) match {
    //        case Some(jv: JValue) => jv
    //        case None => super.renderPipeline(a)
    //      }
    case jv: JsResult[T] =>
      // JSON is always UTF-8
      response.characterEncoding = Some(Codec.UTF8.name)
      val writer = response.writer
      status = 200
      jv match {
        case model: JsSuccess[T] => Json.toJson[T](model.get).toString
        case e: JsError => writer.write(JsError.toFlatForm(e).toString)
      }

      ()
  }: RenderPipeline) orElse super.renderPipeline


  def renderJson[T](model: T)(implicit fmt: Format[T]): String = {
    Json.toJson[T](model).toString
  }

  def parsedModel[T](implicit fmt: Format[T]): JsResult[T] = {
    val result: JsResult[T] = Json.parse(request.body).validate[T]
    result
  }

  def getParsedModel[T] = request.get(ParsedModelKey).orElse(None)
}

