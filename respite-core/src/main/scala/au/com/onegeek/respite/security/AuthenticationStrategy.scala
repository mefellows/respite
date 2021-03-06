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

import scala.concurrent.{ExecutionContext, Future}
import au.com.onegeek.respite.models.ApiKey

/**
 * An API Authentication Strategy for the Authentication Trait.
 *
 * Not to be confused with Authorisation, which is something the `OAuth2Support` Mixin supports.
 *
 * The default Authentication strategy is to use an in-memory Map of Authentication keys, supplied by the environment itself.
 *
 * Created by mfellows on 1/07/2014.
 */
trait AuthenticationStrategy {
  def authenticate(appName: String, apiKey: String)(implicit ec: ExecutionContext): Future[Option[ApiKey]]
  def revokeKey(apiKey: String)(implicit ec: ExecutionContext): Future[Option[ApiKey]]
  def createKey(apiKey: ApiKey)(implicit ec: ExecutionContext): Future[Option[ApiKey]]
  def getKeys(implicit ec: ExecutionContext): Future[List[ApiKey]]
}
