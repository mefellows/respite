import au.com.onegeek.respite.controllers.RestController
import au.com.onegeek.respite.controllers.support.{CachingSupport, MetricsRestSupport, MetricsSupport, MetricsController}
import au.com.onegeek.respite.examples._
import au.com.onegeek.respite.config.{ApiDatasource, ProductionConfigurationModule}
import au.com.onegeek.respite.examples.models._
import au.com.onegeek.respite.models.ApiKey
import au.com.onegeek.respite.security.{MongoDatabaseAuthServlet, DatabaseAuthenticationStrategy, Authentication, AuthenticationApi}
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.servlets.{HealthCheckServlet, MetricsServlet, AdminServlet}
import org.scalatra._
import javax.servlet.{ServletRegistration, ServletContext}

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.MongoConnector

import scala.reflect.ClassTag

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    import au.com.onegeek.respite.models.ModelJsonExtensions._

    val databaseName = "respite"
    val metricsPath = "/metrics"
    val mongoUri: String = s"mongodb://127.0.0.1:17017/$databaseName"

    implicit val mongoConnectorForTest = new MongoConnector(mongoUri)

    implicit val mongo = mongoConnectorForTest.db

    implicit val bindingModule = ProductionConfigurationModule

    context.mount(new RespiteExamples, "/*")
    context.mount(new SimpleAuthServlet, "/auth/*")
    context.mount(new UserController(new UserRepository), "/users")
    context.mount(new RestController[Product, BSONObjectID]("products", Product.format, new ProductRepository) with MetricsRestSupport[Product, BSONObjectID], "/products")
    context.mount(new MetricsController(metricsPath), metricsPath)
  }
}