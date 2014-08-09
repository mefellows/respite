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
  implicit val cache: spray.caching.Cache[T] = new spray.caching.ExpiringLruCache[T](maxCapacity = 500,
    initialCapacity = 16,
    timeToLive = Duration.Inf,
    timeToIdle = Duration.Inf) //with Cache[T]

}


/**
 * Automatically cache CRUD and idempotent routes plus a handy Caching DSL.
 *
 * Note that use of this will automatically convert your routes into {{{FutureSupport}}} routes as cache retrieval
 * is asynchronous.
 *
 */
trait CachingRouteSupport extends ScalatraBase with LoggingSupport with CachingSupport[Route] { this: FutureSupport =>

  val listCache: spray.caching.Cache[Any] = LruCache()

  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route =  {
      val path = transformers.foldLeft("")((path, transformer) => path.concat(transformer.toString()))
      logger.debug(s"Caching path $path on ${getClass}")

      method match {
        case Get | Options | Head =>

            // How to map params etc.? Some Path's will be implemented where params aren't captured in signature.

            // Hash the request params? => This could be a security issue if known i.e. slam site with squillions of combinations of k/v and consume memory

            // Also, don't cache 40x/50x errors?
            //          cachey("oeu") {
//            super.addRoute(method, transformers, listCache(request.pathInfo) { logger.debug("Caching initial call"); action })
            super.addRoute(method, transformers, {
//              val key = s"${request.getMethod}${request.pathInfo}${request.parameters.foldLeft()}"
              val key = s"${request.getMethod}${request.pathInfo}}"
              logger.debug(s"Returning cached route $path on path ${request.pathInfo} in class ${getClass} with key ${key}")
              listCache(key) {
                action
              }
            })
            //          }

        case _ =>
          super.addRoute(method, transformers, action)
      }
    }


  delete("/cache/expire") {
    listCache.clear()
  }

//    super.addRoute(method, transformers, action)
//  }

//  implicit val cacheProvider: CachingStrategy = SprayCachingStrategy



  // Review the cruddy Cache implementation in the Controllers

  // Add in pluggable caching implementation (default - in-memory Spray caching. Make sure it manages itself. Allow EHCache or Memcached etc.


  // See below for inspiration / example
  // https://github.com/playframework/playframework/blob/026e28348c92dab1f7967089bd40631b98f9d2e2/framework/src/play-cache/src/main/scala/play/api/cache/Cache.scala


//  /**
//   * Retrieve a value from the cache, or set it from a default function.
//   *
//   * @param key Item key.
//   * @param expiration expiration period in seconds.
//   * @param orElse The default function to invoke if the value was not found in cache.
//   */
//  def getOrElse[A](key: String, expiration: Int = 0)(orElse: => A)(implicit app: Application, ct: ClassTag[A]): A = {
//    getAs[A](key).getOrElse {
//      val value = orElse
//      set(key, value, expiration)
//      value
//    }
//  }

  // don't couple the caching API with specific cache, but do provide a sensible default (In-memory using Spray or Play's API)

}

trait Memoization[T] extends CachingSupport[T]