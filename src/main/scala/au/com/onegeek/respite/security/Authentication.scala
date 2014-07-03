package au.com.onegeek.respite.security

import org.scalatra.{FutureSupport, AsyncResult, ScalatraBase}
import com.escalatesoft.subcut.inject.Injectable
import org.slf4j.LoggerFactory
import reactivemongo.api.DefaultDB
import spray.caching.{LruCache, Cache}
import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future, Await}
import reactivemongo.bson.BSONDocument
import scala.concurrent.duration._

/**
 * Created by mfellows on 11/05/2014.
 */
/**
 * When this trait is used, the incoming request
 * is checked for authentication based on the
 * X-API-Key header.
 */
trait Authentication extends ScalatraBase with Injectable {

  protected implicit def executor: ExecutionContext = ExecutionContext.global

  // Override this strategy for more explicit control
  implicit val authenticationStrategy: AuthenticationStrategy = ConfigAuthenticationStrategy

  val _log = LoggerFactory.getLogger(getClass)
  val API_KEY_HEADER = "X-API-Key";
  val API_APP_HEADER = "X-API-Application";
  val API_ERROR_HEADER = "X-API-Fault-Description";

  /**
   * A simple interceptor that checks for the existence
   * of the correct headers. Down the track, this could also be an OAuth 2.0 implementation
   */
  before() {
    // we check the host where the request is made
    val serverName = request.serverName;
    val header = Option(request.getHeader(API_KEY_HEADER));
    val app = Option(request.getHeader(API_APP_HEADER));

    _log.debug("Looking for some headers..." + header + ", " + app)

    List(app, header) match {
      case List(Some(x), Some(y)) => {
        _log.debug("Awaiting a result...")
        val key = Await.result(authenticationStrategy.authenticate(x, y), 100 millis)

        key match {
          case Some(k) => {
            _log.debug(s"Found key: ${k}")
          }
          case None => {
            _log.debug(s"No or invalid API keys provided. Result of lookup ${key}")
            rejectRequest()
          }
          case _ => {
            _log.debug(s"ok, somethihng else happened")
            rejectRequest()
          }
        }
      }
      case _ => {
        _log.debug("Oh oh, no headers!")
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
  def rejectRequest(reason: String = s"Invalid authentication: X-API-* headers ('${API_KEY_HEADER}', '${API_APP_HEADER}') not provided or invalid") = {
    halt(status = 401, headers = Map("WWW-Authenticate" -> "API-Key", API_ERROR_HEADER -> reason))
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

}

trait AuthenticationApi extends Authentication with FutureSupport { this: Authentication =>

  delete("/key/foo/:key") {
    new AsyncResult {
    _log.debug("Removing a key")

      val key = params.get("key").getOrElse(keyNotFound("Invalid request, no key provided"))
      val is = for {
       result <- authenticationStrategy.revokeKey(key)
      } yield result.orElse(keyNotFound("Invalid or no key provided"))
    }
  }
}