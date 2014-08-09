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

import au.com.onegeek.respite.controllers.support.{LoggingSupport}
import uk.gov.hmrc.mongo.Repository
import au.com.onegeek.respite.models.ApiKey
import reactivemongo.bson.BSONObjectID
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json

/**
 * This class implements a Database persisted Authentication Strategy, with
 * programmatic / API hooks to add/revoke access at runtime.
 *
 * Created by mfellows on 1/07/2014.
 */
class DatabaseAuthenticationStrategy[ObjectID] (repository: Repository[ApiKey, ObjectID]) extends AuthenticationStrategy with LoggingSupport {

  val API_TOKENS_COLLECTION = "apitokens"

  override def authenticate(appName: String, apiKey: String)(implicit ec: ExecutionContext): Future[Option[ApiKey]] = {
    logger.debug(s"Authenticating key: ${apiKey} and app: ${appName}")
      for {
        keys <- repository.find("key" -> apiKey, "application" -> appName)
      } yield keys.headOption
  }

  override def revokeKey(apiKey: String)(implicit ec: ExecutionContext): Future[Option[ApiKey]] = {
    logger.debug(s"Revoking key: ${apiKey}")
      for {
        keys <- repository.find("key" -> apiKey)
        result <- repository.remove("key" -> apiKey)
      } yield keys.headOption
  }

  /**
   * Create's an API Key in the MongoDB repository.
   * @param apiKey
   * @param ec
   * @return
   */
  override def createKey(apiKey: ApiKey)(implicit ec: ExecutionContext): Future[Option[ApiKey]] = {
    logger.debug(s"Creating key: ${apiKey}")
      for {
        result <- repository.insert(apiKey)
      } yield result.ok match {
        case true => Some(apiKey)
        case false => None
      }
  }
}