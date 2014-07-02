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

import au.com.onegeek.respite.controllers.support.CachingSupport
import uk.gov.hmrc.mongo.Repository
import au.com.onegeek.respite.models.ApiKey
import reactivemongo.bson.BSONObjectID
import scala.concurrent.{ExecutionContext, Future}

/**
 * This class implements a Database persisted Authentication Strategy, with
 * programmatic / API hooks to add/revoke access at runtime.
 *
 * Created by mfellows on 1/07/2014.
 */
class DatabaseAuthenticationStrategy(repository: Repository[ApiKey, BSONObjectID]) extends AuthenticationStrategy with CachingSupport {

  val API_TOKENS_COLLECTION = "apitokens"

  override def authenticate(appName: String, apiKey: String)(implicit ec: ExecutionContext): Future[Option[ApiKey]] = {
    println(s"Authenticating key: ${apiKey} and app: ${appName}")
      for {
        keys <- repository.find("key" -> apiKey, "application" -> appName)
      } yield Some(keys.head)
  }

  override def revokeKey(apiKey: String)(implicit ec: ExecutionContext): Future[Option[ApiKey]] = {
    println(s"Revoking key: ${apiKey}")
      for {
        keys <- repository.find("key" -> apiKey)
      } yield Some(keys.head)
  }
}