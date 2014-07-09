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
import org.scalatra.util.conversion.TypeConverter

/**
 * Created by mfellows on 27/06/2014.
 */
object PlayJsonSupport {
  val ParsedModelKey = "au.com.onegeek.respite.parsedModel"
}

@implicitNotFound(
  "No play JSON Reads/Writes defaults are in place. Try adding au.com.onegeek.respite.database.DefaultFormats or creating your own."
)
trait PlayJsonSupport[T] extends ScalatraBase  with ApiFormats { this: ApiFormats =>

  import PlayJsonSupport._

  /**
   * Set content-type header to JSON.
   */
  before() {
    contentType = formats("json")
  }

  // Hoping to be able to go back to this to avoid passing Type Parameters around in all subclasses. OK for now.
  //  implicit val formats: JsonFormats
  implicit val format: Format[T]

  private def shouldParseBody(fmt: String)(implicit request: HttpServletRequest) = fmt == "json"

  override protected def invoke(matchedRoute: MatchedRoute) = {

    withRouteMultiParams(Some(matchedRoute)) {


      // TODO: ONly support POST?

      val mt = request.contentType map {
        _.split(";").head
      } getOrElse "application/x-www-form-urlencoded"

      val fmt = mimeTypes get mt getOrElse "html"

      // Extract  the JSON request body if the message is a JSON object
      if (shouldParseBody(fmt)) {
        //      Option(request.body) filterNot {_.isEmpty} map {s => request(ParsedModelKey) = Some(Json.parse(request.body).validate[T])}
        Option(request.body) filterNot { _.isEmpty } map { s =>
          val validation = Json.parse(s).validate[T]
          request(ParsedModelKey) = validation
        }
      }

      super.invoke(matchedRoute)
    }
  }

  override protected def renderPipeline = ({
    // Note the following from the JValueResult class -> Secret sauce is PartialFunctions?
    case jv: JsResult[T] =>
      val writer = response.writer
      // JSON is always UTF-8
      response.characterEncoding = Some(Codec.UTF8.name)
      status = 200
      jv match {
        case model: JsSuccess[T] => writer.write(Json.toJson[T](model.get).toString)
        case e: JsError =>
          status = 400
          writer.write(JsError.toFlatJson(e).toString)
      }
      ()
      // Presumably due to Type Erasure, this never fires - Option[JsResult[T]] does first!
    case a: Option[T] if responseFormat == "json" =>
      a match {
        case Some(model) => response.writer.write(renderJson(model))
        case None => doNotFound
      }
      ()
    case jv: Option[JsResult[T]] =>
      val writer = response.writer
      // JSON is always UTF-8
      response.characterEncoding = Some(Codec.UTF8.name)
      status = 200
      jv.get match {
        case model: JsSuccess[T] => writer.write(Json.toJson[T](model.get).toString)
        case e: JsError =>
          writer.write(JsError.toFlatJson(e).toString)
          status = 400
      }
      ()
    case list: List[T] if responseFormat == "json" =>
      response.writer.write(renderJson(list))
      ()
    case a: T if responseFormat == "json" =>
      response.writer.write(renderJson(a))
      ()
  }: RenderPipeline) orElse super.renderPipeline


  def renderJson[T](model: JsResult[T])(implicit fmt: Format[T]): String = {
    Json.toJson[T](model.get).toString
  }

  def renderJson[T](model: T)(implicit fmt: Format[T]): String = {
    Json.toJson[T](model).toString
  }

  def parsedModel[T](implicit fmt: Format[T]): JsResult[T] = {
    val result: JsResult[T] = Json.parse(request.body).validate[T]
    result
  }

  /**
   * Get the model submitted via a JSON POST.
   *
   * @tparam T The Generic type of object posted.
   * @return
   */
  def getParsedModel[T]: Option[JsResult[T]] = request.get(ParsedModelKey) map { o =>
    o.asInstanceOf[JsResult[T]]
  }
}