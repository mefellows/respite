import au.com.onegeek.respite.config.ProductionConfigurationModule
import au.com.onegeek.respite.controllers.RestController
import au.com.onegeek.respite.examples._
import au.com.onegeek.respite.examples.models._
import org.scalatra._
import javax.servlet.ServletContext

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.MongoConnector

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    import au.com.onegeek.respite.models.ModelJsonExtensions._

    val databaseName = "respite"

    val mongoUri: String = s"mongodb://127.0.0.1:17017/$databaseName"

    implicit val mongoConnectorForTest = new MongoConnector(mongoUri)

    implicit val mongo = mongoConnectorForTest.db

    implicit val bindingModule = ProductionConfigurationModule

    context.mount(new RespiteExamples, "/*")
    context.mount(new UserController(new UserRepository), "/users")
    context.mount(new RestController[Product, BSONObjectID]("products", Product.format, new ProductRepository), "/products/*")
  }
}
