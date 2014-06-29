package au.com.respite.api.controllers

import scala.concurrent._
import reactivemongo.bson.BSONDocument
import scala.util.{Success, Failure}
import org.json4s.DefaultFormats
import org.scalatra.AsyncResult
import au.com.respite.api.models.AccountComponents._
import au.com.respite.api.config.TestConfigurationModule
import com.escalatesoft.subcut.inject.BindingModule
import au.com.respite.api.models.JsonFormats._
import au.com.respite.api.models.DefaultFormats._

class UsersController(implicit override val bindingModule: BindingModule) extends RestController[User]("users") {

  // Do something specific, override etc.


}