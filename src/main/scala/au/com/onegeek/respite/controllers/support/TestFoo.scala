package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.models.AccountComponents.User
import au.com.onegeek.respite.models.ApiKey
import play.api.libs.json.Json

/**
 * Created by mfellows on 3/07/2014.
 */
object TestFoo extends TestFoo
trait TestFoo {
  import uk.gov.hmrc.mongo.ReactiveMongoFormats._
  implicit val ApiKeysJsonFormat = Json.format[ApiKey]
  implicit val UserJsonFormat = Json.format[User]
}
