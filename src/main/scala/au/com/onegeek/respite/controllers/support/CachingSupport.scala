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

/**
 * Created by mfellows on 23/06/2014.
 */
class CachingSupport {

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
