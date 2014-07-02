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
package au.com.onegeek.respite.security

import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import au.com.onegeek.respite.controllers.support.CachingSupport
import uk.gov.hmrc.mongo.{Repository, ReactiveRepository}
import org.scalatra.ScalatraServlet
import au.com.onegeek.respite.models.{ApiKey, Repository}

/**
 * This class implements a Database persisted Authentication Strategy, with
 * programmatic / API hooks to add/revoke access at runtime.
 *
 * Created by mfellows on 1/07/2014.
 */
trait DatabaseAuthenticationStrategy extends AuthenticationStrategy with CachingSupport {

  val repository: Repository
  val API_TOKENS_COLLECTION = "apitokens"

  /**
   * Check whether the secure API request is valid.
   *
   * This is done by checking the host against a database of keys.
   */
//  final def findKey(appName: String, apiKey: String): Future[Option[BSONDocument]] = keyCache(appName+apiKey) {
  def authenticate(appName: String, apiKey: String): Future[Option[ApiKey]] = {
//    _log.debug(s"Looking for a key...$appName, $apiKey")
    val query = BSONDocument("app" -> appName, "key" -> apiKey)
//    val collection = db(API_TOKENS_COLLECTION)
    collection.find(query).one
  }

  def removeKey(appName: String, apiKey: String): Future[Option[ApiKey]] = {
//    _log.debug(s"Removing key ${appName}, ${apiKey}}")
    val query = BSONDocument("app" -> appName, "key" -> apiKey)
//    val collection = db(API_TOKENS_COLLECTION)
    repository.remove()
    collection.remove(query)

    keyCache.remove(appName+apiKey)
  }
}
