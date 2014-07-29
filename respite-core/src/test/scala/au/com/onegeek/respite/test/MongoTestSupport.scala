package au.com.onegeek.respite.test

import uk.gov.hmrc.mongo.MongoConnector
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
/**
 * Created by mfellows on 2/07/2014.
 */
trait MongoSpecSupport {

  protected val databaseName = "test" + this.getClass.getSimpleName.toLowerCase

  protected val databasePort = sys.env.get("MONGO_PORT").map( port => port.toInt ).getOrElse(17017)

  protected lazy val mongoUri: String = s"mongodb://127.0.0.1:$databasePort/$databaseName"

  implicit lazy val mongoConnectorForTest = new MongoConnector(mongoUri)

  implicit lazy val mongo = mongoConnectorForTest.db

  def bsonCollection(name: String)(failoverStrategy: FailoverStrategy = mongoConnectorForTest.helper.db.failoverStrategy): BSONCollection = {
    mongoConnectorForTest.helper.db(name, failoverStrategy)
  }

}
trait Awaiting {

  import scala.concurrent._

  implicit val ec = ExecutionContext.Implicits.global

  val timeout = 5 seconds

  def await[A](future: Future[A])(implicit timeout: Duration = timeout) = Await.result(future, timeout)
}