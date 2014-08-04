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

import au.com.onegeek.respite.controllers.RestController
import au.com.onegeek.respite.controllers.support.{PlayJsonSupport, LoggingSupport}
import au.com.onegeek.respite.models.ApiKey
import org.scalatra.{FutureSupport, AsyncResult, ScalatraBase}
import com.escalatesoft.subcut.inject.Injectable
import org.slf4j.LoggerFactory
import play.api.libs.json.{Format, JsError, Json, JsSuccess}
import reactivemongo.api.DefaultDB
import spray.caching.{LruCache, Cache}
import java.util.concurrent.TimeUnit
import scala.concurrent._
import reactivemongo.bson.BSONDocument
import scala.concurrent.duration._


/**
 * Authenticate Scalatra Servlets with an [[au.com.onegeek.respite.models.ApiKey]] via the 'X-API-Key' and 'X-API_Application' headers.
 *
 */
trait Authentication extends ScalatraBase with Injectable with LoggingSupport {

  protected implicit def executor: ExecutionContext = ExecutionContext.global

  // Override this strategy for more explicit control
  implicit val authenticationStrategy: AuthenticationStrategy

  val API_KEY_HEADER = "X-API-Key";
  val API_APP_HEADER = "X-API-Application";
  val API_ERROR_HEADER = "X-API-Fault-Description";
  val BLOCKING_TIMEOUT = 100 millis

  /**
   * A simple interceptor that checks for the existence
   * of the correct headers. Down the track, this could also be an OAuth 2.0 implementation
   */
  before() {
    // we check the host where the request is made
    val serverName = request.serverName;
    val header = Option(request.getHeader(API_KEY_HEADER));
    val app = Option(request.getHeader(API_APP_HEADER));

    logger.debug("Looking for some headers..." + header + ", " + app)

    List(app, header) match {
      case List(Some(appName), Some(appKey)) => {
        try {
          logger.debug(s"Looking up key '$appKey' from AuthenticationStrategy '$authenticationStrategy")
          val key = Await.result(authenticationStrategy.authenticate(appName, appKey), BLOCKING_TIMEOUT)

          key match {
            case Some(k) => {
              logger.debug(s"Found key: ${k}")
            }
            case None => {
              logger.debug(s"No or invalid API keys provided. Result of lookup ${key}")
              rejectRequest()
            }
          }
        } catch {
          case e: TimeoutException => rejectRequest(status = 503, reason = "Temporarily unable to authenticate")
        }
      }
      case _ => {
        logger.debug("Oh oh, no headers!")
        rejectRequest()
      }
    }
  }

  /**
   * Rejects an API request with the standard 40x header and a human-friendly response message.
   *
   * @param reason
   * @return
   */
  def rejectRequest(status: Integer = 401, reason: String = s"Invalid authentication: X-API-* headers ('${API_KEY_HEADER}', '${API_APP_HEADER}') not provided or invalid") = {
    halt(status = status, headers = Map("WWW-Authenticate" -> "API-Key", API_ERROR_HEADER -> reason))
  }

  /**
   * Rejects an API request with the standard 40x header and a human-friendly response message.
   *
   * @param reason
   * @return
   */
  def keyNotFound(reason: String = s"Key provided not found") = {
    halt(status = 404, reason = reason)
  }

  /**
   * Rejects an API request with the standard 40x header and a human-friendly response message.
   *
   * @param reason
   * @return
   */
  def keyExists(reason: String = s"Key already exists") = {
    halt(status = 400, reason = reason)
  }

}

trait AuthenticationApi extends Authentication with FutureSupport with PlayJsonSupport[ApiKey] {

  override implicit val format = ApiKey.format
  /**
   *
   */
  delete("/token/:key") {
    new AsyncResult {
    logger.debug("Removing a key")

      val key = params.get("key").get
      val is = for {
       result <- authenticationStrategy.revokeKey(key)
      } yield result.orElse(keyNotFound("Invalid or no key provided"))
    }
  }

  post("/token/?") {
    logger.debug("Creating a token")
    new AsyncResult() {
      val token = parsedModel[ApiKey]
      val is = for {
        result <- authenticationStrategy.createKey(token.get)
      } yield result.orElse(keyExists())
    }
  }
}