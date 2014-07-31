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

import nl.grons.metrics.scala._
import org.scalatra._
import au.com.onegeek.respite.controllers.RestController
import reactivemongo.bson.BSONObjectID
import com.escalatesoft.subcut.inject.BindingModule
import scala.reflect.ClassTag
import javax.servlet.http.HttpServletRequest
import au.com.onegeek.respite.models.Model
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

object RespiteApplicationMetrics {
  val healthChecksRegistry = new com.codahale.metrics.health.HealthCheckRegistry();
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
}

/**
* Metrics and HealthCheck Support.
*
* Mixin this Trait into any Class, Controller etc. to gain access to A metrics
* and health-check DSL, provided by https://github.com/erikvanoosten/metrics-scala/.
*
* {{{RestControllers}}} have this out-of-the-box so is only required on non-Respite
* family objects.
*
* Created by mfellows on 23/06/2014.
*/
trait Metrics extends InstrumentedBuilder with CheckedBuilder {
  override lazy val metricBaseName = MetricName(getClass)
  val metricRegistry = RespiteApplicationMetrics.metricRegistry
  val registry = RespiteApplicationMetrics.healthChecksRegistry
}

/**
 *
 */
trait MetricsSupport extends ScalatraBase with Metrics with LoggingSupport {

  // Lazy registry of Metric machines to avoid initialisation
  // TODO: This could probably be made eager?
  // Actually, it should be created during Scalatra initialisation during all of the 'addRoute' invocations
  private[this] val loaders = Map[String, Timer]()
  private[this] val counters = Map[String, Counter]()

  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val path = transformers.foldLeft("")((path, transformer) => path.concat(transformer.toString()))
    val name = path match {
      case "/" if method == Get => "list"
      case "/:id" if method == Get => "single"
      case "/" if method == Post => "create"
      case "/:id" if method == Post => "update"
      case "/:id" if method == Put => "update"
      case "/:id" if method == Delete => "delete"
      case _ => path.replaceAll("[^a-zA-Z0-9_-]", "") // Ideally capitalise/camelCase this. Also avoid collisions from above.
    }

    logger.debug(s"Instrumenting path $path on ${metricBaseName.name}")

    super.addRoute(method, transformers, {
      // Wrap the action in a timer
      metrics.timer(method.toString.toLowerCase, name).time {
        logger.trace(s"Instrumenting $action")
        action
        logger.trace(s"Instrumentation of $action complete")
      }
    })
  }
}

/**
 * RESTful CRUD specific Metrics Support. Mix this in when using `RestController`
 */
trait MetricsRestSupport[ObjectType <: Model[ObjectID], ObjectID] extends MetricsSupport { this: RestController[ObjectType, ObjectID] =>

  val REST_CLASSNAME = "au.com.onegeek.respite.controllers.RestController"
  // Metrics - override metrics base name if controller has not been subclassed (i.e. direct instantiation)
  override lazy val metricBaseName = {
    val ANON = ".*\\$\\$anon\\$.*"
    getClass.getName match {
      case REST_CLASSNAME                     => MetricName(s"RestController.${collectionName.capitalize}")
      case name: String if name.matches(ANON) => MetricName(s"RestController.${collectionName.capitalize}")
      case name: String                       => MetricName(name)
    }
  }

  healthCheck("get.list", s"List all entities in $metricBaseName failed") {
    // Call Route by name
//    val is = Await.result( {this.actor ? "all"}, 100 millis)

    implicit def executor: ExecutionContext = ExecutionContext.global


    val is = this.actor ? "all"
    println(is)

    true
  }
}