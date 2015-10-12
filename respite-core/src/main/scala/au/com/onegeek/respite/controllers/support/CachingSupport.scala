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

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatra._

import scala.concurrent.duration._

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
trait CachingRouteSupport extends ScalatraBase with LoggingSupport with CachingSupport[Any] with FutureSupport {
  val YEAR_IN_MINUTES = 365 * 24 * 60
  val YEAR_IN_DAYS = 365

  def sortRequestParameters(): Map[String, String] = request.parameters.toSeq.sortBy(_._1).toMap
  def getCacheKey(): String = s"${request.getMethod}${request.pathInfo}" + (sortRequestParameters.foldLeft(""){ case(builder, (k, v)) => s"${builder},${k}=${v}" }).replaceAll("^,", "")

  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val path = transformers.foldLeft("")((path, transformer) => path.concat(transformer.toString()))
    logger.debug(s"Caching path $path on ${getClass}")

    method match {

      // Equates to GET, HEAD, OPTIONS, CONNECT, TRACE
      case m: HttpMethod if m.isSafe =>
        super.addRoute(method, transformers, {

          val key = getCacheKey()

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
      case Post | Put | Patch | Delete =>
        super.addRoute(method, transformers, {
            HttpMethod.methods.filter(_.isSafe).foreach { m =>
              val key = getCacheKey.replaceFirst(method.toString, m.toString)
              logger.info(s"Evicting cache key ${key} due to unsafe op")
              cache.remove(key)
            }
          action
        })
      case _ =>
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