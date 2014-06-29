import _root_.akka.actor.ActorSystem
import au.com.respite.api.config.ProductionConfigurationModule
import au.com.respite.api.controllers.{RestController, UsersController}
import au.com.respite.api.models.AccountComponents.{User, Foo}
import org.scalatra._
import javax.servlet.ServletContext
import org.slf4j.LoggerFactory
import reactivemongo.api.MongoDriver
import scala.concurrent.ExecutionContext
import scala.util.Success
import au.com.respite.api.models.JsonFormats._
import au.com.respite.api.models.DefaultFormats._

class ScalatraBootstrap extends LifeCycle {
  protected implicit def executor: ExecutionContext = ExecutionContext.global

  val logger = LoggerFactory.getLogger(getClass)

  // Add implicit Binding Module in here....

  // Get a handle to an ActorSystem and a reference to one of your actors
  val system = ActorSystem()
  override def init(context: ServletContext) {
    implicit val bindingModule = ProductionConfigurationModule

//    context.mount(new UsersController, "/users/*")
    context.mount(new RestController[User]("users"), "/users/*")
  }

  // Make sure you shut down
  override def destroy(context:ServletContext) {
    system.shutdown()
  }
}

