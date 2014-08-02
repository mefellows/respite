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

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import nl.grons.metrics.scala._
import au.com.onegeek.respite.controllers.RestController
import org.scalatra._
import reactivemongo.bson.BSONObjectID
import com.escalatesoft.subcut.inject.BindingModule
import scala.reflect.ClassTag
import javax.servlet.http.HttpServletRequest
import au.com.onegeek.respite.models.Model
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import java.util.concurrent.TimeoutException

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
  def getTimer(path: String, method: HttpMethod): Timer = {
    path match {
      case "/" if method == Get => metrics.timer("list")
      case "/:id" if method == Get => metrics.timer("single")
      case "/" if method == Post => metrics.timer("create")
      case "/:id" if method == Post => metrics.timer("update")
      case "/:id" if method == Put => metrics.timer("update")
      case "/:id" if method == Delete => metrics.timer("delete")
      // Ideally capitalise/camelCase this. Also avoid collisions from above.
      case _ if !path.toString.replaceAll("[^a-zA-Z0-9_-]", "").isEmpty => metrics.timer(method.toString.toLowerCase, path.replaceAll("[^a-zA-Z0-9_-]", ""))
      case _ => metrics.timer(method.toString.toLowerCase)
    }
  }

  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val path = transformers.foldLeft("")((path, transformer) => path.concat(transformer.toString()))

    logger.debug(s"Instrumenting path $path on ${metricBaseName.name}")

    super.addRoute(method, transformers, {
      getTimer(path, method).time {
        logger.debug(s"Instrumenting Action")
        action
      }
    })
  }
}

/**
 * RESTful CRUD specific Metrics Support. Mix this in when using `RestController`
 */
trait MetricsRestSupport[ObjectType <: Model[ObjectID], ObjectID] extends MetricsSupport { this: RestController[ObjectType, ObjectID] =>

  val REST_CLASSNAME        = "au.com.onegeek.respite.controllers.RestController"
  val HEALTH_CHECK_TIMEOUT  = 100 millis

  // Metrics - override metrics base name if controller has not been subclassed (i.e. direct instantiation)
  override lazy val metricBaseName = {
    val ANON = ".*\\$\\$anon\\$.*"
    getClass.getName match {
      case REST_CLASSNAME                     => MetricName(s"RestController.${collectionName.capitalize}")
      case name: String if name.matches(ANON) => MetricName(s"RestController.${collectionName.capitalize}")
      case name: String                       => MetricName(name)
    }
  }

  /**
   * Create default health check on REST controller - confirm CRUD pipelines are active
   * (what exactly does 'active' mean?)
   */
  healthCheck("list", s"List all entities in $metricBaseName failed") {
    implicit def executor: ExecutionContext = ExecutionContext.global

    def check(): Boolean = {
      try {
        val futureList = Await.result({
          this.actor ? "all"
        }, HEALTH_CHECK_TIMEOUT).asInstanceOf[Future[List[ObjectType]]]

        Await.result(futureList, HEALTH_CHECK_TIMEOUT) match {
          case l: List[ObjectType] => true
          case _ => false
        }
      } catch {
        case _: Throwable => false
      }
    }

    // See https://github.com/erikvanoosten/metrics-scala/blob/master/docs/HealthCheckManual.md#warning-for-version-31x-and-earlier
    check()
  }
}