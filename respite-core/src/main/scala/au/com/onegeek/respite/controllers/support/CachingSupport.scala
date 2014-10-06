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

import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

import nl.grons.metrics.scala.Timer
import org.scalatra._
import spray.caching.{LruCache, ExpiringLruCache}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.DateTimeFormat

/**
 * Models a canonical Caching interface for use by the Caching Abstraction
 */
trait Cache[T] extends spray.caching.Cache[T] {

}

trait SprayCache[T] extends Cache[T] {

}

/**
 * Generic caching DSL for objects and Routes.
 */
trait CachingSupport[T] {
  val timeToLive: Duration = Duration.Inf
  val timeToIdle: Duration = Duration.Inf
  val maxCapacity: Int = 500
  val initialCapacity: Int = 16

  lazy val cache: spray.caching.Cache[T] = new spray.caching.ExpiringLruCache[T](maxCapacity = maxCapacity,
    initialCapacity = initialCapacity,
    timeToLive = timeToLive,
    timeToIdle = timeToIdle)
}


/**
 * Automatically cache CRUD and idempotent routes plus a handy Caching DSL.
 *
 * Note that use of this will automatically convert your routes into {{{FutureSupport}}} routes as cache retrieval
 * is asynchronous.
 *
 * NOTES:
 *
 *  If the caching is set to 365 days or greater (including 'infinity') the Expires header is set to exactly 365 days in the future.
 *  The Cache can be set to 'auto-evict' meaning
 *
 */
trait CachingRouteSupport extends ScalatraBase with LoggingSupport with CachingSupport[Any] {
  this: FutureSupport =>

  val YEAR_IN_MINUTES = 365 * 24 * 60
  val YEAR_IN_DAYS = 365

  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val path = transformers.foldLeft("")((path, transformer) => path.concat(transformer.toString()))
    logger.debug(s"Caching path $path on ${getClass}")

    method match {

      // Equates to GET, HEAD, OPTIONS, CONNECT, TRACE
      case m: HttpMethod if m.isSafe =>

        // How to map params etc.? Some Path's will be implemented where params aren't captured in signature.

        // Hash the request params? => This could be a security issue if known i.e. slam site with squillions of combinations of k/v and consume memory

        // Also, don't cache 40x/50x errors?

        // TODO: Evaluate Cache Request Request Directives - i.e. ignore/bypass cache etc.

        // Note: Sensitivity is reduced to minutes
        super.addRoute(method, transformers, {

          // Properly flatten k/v into the key
          val key = s"${request.getMethod}${request.pathInfo}${request.parameters flatMap {case(k, v) => k + "=" + v} mkString}"

          logger.debug(s"Returning cached route $path on path ${request.pathInfo} in class ${getClass} with key ${key}")
          cache(key) {

            // Setup Caching Response Headers according to RFCs:
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9 and
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21
            val fmt = DateTimeFormat.forPattern("E, d MMM y kk:mm:ss");
            val minutes = if (!timeToLive.isFinite || timeToLive.toDays > YEAR_IN_DAYS) YEAR_IN_MINUTES else timeToLive.toMinutes.toInt
            response.setHeader("Cache-Control", "Public")
            response.setHeader("Expires", fmt.print(DateTime.now.plusMinutes(minutes).withZone(DateTimeZone.UTC)) + " GMT")

            // Return Body
            action
          }
        })

      case _ =>
        // TODO: Invalidate cache entries on PUT/POST/DELETE?
        super.addRoute(method, transformers, action)
    }
  }

  delete("/cache/") {
    cache.clear()
  }

  delete("/cache/:key") {
    val key = params.get("key").get
    cache.remove(key)
  }
}