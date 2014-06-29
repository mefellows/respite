package au.com.respite.api.controllers

import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import javax.servlet.http.HttpServletRequest
import collection.mutable
import org.json4s.{Formats, DefaultFormats}
import org.scalatra.json.{JValueResult, JacksonJsonSupport}

//trait RespiteApiStack extends ScalatraServlet with ScalateSupport with JacksonJsonSupport with JValueResult with SessionSupport {
trait RespiteApiStack extends ScalatraServlet {

  implicit protected val jsonFormats: Formats = DefaultFormats

  notFound {
    resourceNotFound()
  }
}
