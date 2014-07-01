package au.com.onegeek.respite.controllers.support

import au.com.onegeek.respite.api.ServletTestsBase
import org.scalatest.concurrent.ScalaFutures
import au.com.onegeek.respite.config.TestConfigurationModule
import com.escalatesoft.subcut.inject.Injectable
import org.scalatra.ScalatraServlet
import au.com.onegeek.respite.models.AccountComponents.User
import au.com.onegeek.respite.models.DefaultFormats
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.JsSuccess
import scala.Some
import au.com.onegeek.respite.models.AccountComponents.User
import scala.Tuple2
import uk.gov.hmrc.mongo._

case class NestedModel(a: String, b: String)

case class TestObject(aField: String,
                      anotherField: Option[String] = None,
                      optionalCollection: Option[List[NestedModel]] = None,
                      nestedMapOfCollections: Map[String, List[Map[String, Seq[NestedModel]]]] = Map.empty,
                      modifiedDetails: CreationAndLastModifiedDetail = CreationAndLastModifiedDetail(),
                      jsValue: Option[JsValue] = None,
                      location : Tuple2[Double, Double] = (0.0, 0.0),
                      id: BSONObjectID = BSONObjectID.generate) {

  def markUpdated(implicit updatedTime: DateTime) = copy(
    modifiedDetails = modifiedDetails.updated(updatedTime)
  )

}

object TestObject {

  import ReactiveMongoFormats.mongoEntity


  implicit val formats = mongoEntity {

    implicit val locationFormat = TupleFormats.tuple2Format[Double, Double]

    implicit val nestedModelformats = Json.format[NestedModel]

    import ReactiveMongoFormats.objectIdFormats

    Json.format[TestObject]
  }
}

class SimpleTestRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[TestObject, BSONObjectID]("simpleTestRepository", mc.db, TestObject.formats, ReactiveMongoFormats.objectIdFormats) {

  import reactivemongo.api.indexes.IndexType
  import reactivemongo.api.indexes.Index

  override def ensureIndexes() = {
    collection.indexesManager.ensure(Index(Seq("aField" -> IndexType.Ascending), name = Some("aFieldUniqueIdx"), unique = true, sparse = true))
  }
}
import scala.concurrent.duration._
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.default.BSONCollection

trait MongoSpecSupport {

  protected val databaseName = "test" + this.getClass.getSimpleName.toLowerCase

  protected val mongoUri: String = s"mongodb://127.0.0.1:17017/$databaseName"

  implicit val mongoConnectorForTest = new MongoConnector(mongoUri)

  implicit val mongo = mongoConnectorForTest.db

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
/**
 * Created by mfellows on 29/06/2014.
 */
class PlayJsonSupportSpec extends ServletTestsBase with ScalaFutures with MongoSpecSupport with Awaiting with CurrentTime {
  implicit val bindingModule = TestConfigurationModule

  class TestServlet extends ScalatraServlet

  val playServlet = new TestServlet with PlayJsonSupport[User] {
//  override implicit val formats: JsonFormats = DefaultFormats
     implicit val format: Format[User] = DefaultFormats.UserJsonFormat

    get("/") {
      JsSuccess(User(username="foo", firstName = "bar"))
    }

    post("/") {
      val postBody = request.get("myspecialkey")
      println(postBody)
      val u: JsResult[User] = parsedModel[User]

      u match {
        case model: JsSuccess[User] => {
            model.get.firstName
        }
        case e: JsError => {
          JsError.toFlatJson(e)
        }
      }
    }

    post("/foo") {
      val postBody = request.get("myspecialkey")
      getParsedModel
    }

    override def initialize(config: ConfigT) {
      super.initialize(config)
    }
  }

  addServlet(playServlet, "/*")

  "A JSON support servlet" should {

    "Transparently convert Models into JSON" in {
      get("/") {
        status should equal(200)
        println(body)
      }
    }

    "Convert Lists of Models into JSON" in {

    }

    "Stay out of the way for non-JSON requests" in {

    }

    "Fail invalid JSON requests" in {

    }

    "Store the validated Model object in the Request map" in {
      post("/", "{\"_id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}", Map("Content-Type" -> "application/json")) {
        status should equal(200)
        body should equal ("Matt")

        ()
      }
    }

    "Store a JsValue in the Request Map" in {
      post("/foo", "{\"_id\":{\"$oid\":\"53af77a90100000100a16ffb\"},\"username\":\"mfellows\",\"firstName\":\"Matt\"}", Map("Content-Type" -> "application/json")) {
        status should equal(200)
        ()
      }
    }

    "Store stuff in repo" in {
      import ReactiveMongoFormats.objectIdFormats
      val repository = new SimpleTestRepository

      val e1 = TestObject("1")
      val e2 = TestObject("2", optionalCollection = Some(List(NestedModel("A", "B"), NestedModel("C", "D"))))
      val e3 = TestObject("3", nestedMapOfCollections = Map(
        "level_one" -> List(
          Map("level_two_1" -> Seq(NestedModel("A1", "B1"), NestedModel("C1", "D1"), NestedModel("E1", "F1"))),
          Map("level_two_2" -> Seq(NestedModel("A2", "B2"))),
          Map("level_two_3" -> Seq(NestedModel("A1", "B1"), NestedModel("C1", "D1"), NestedModel("E1", "F1"), NestedModel("G2", "H2")))
        )
      ))
      val e4 = TestObject("4")

      val created = for {
        res1 <- repository.save(e1)
        res2 <- repository.save(e2)
        res3 <- repository.save(e3)
        countResult <- repository.count
      } yield countResult

      await(created) shouldBe 3

      val result: List[TestObject] = await(repository.findAll)
      result.foreach (println)
      result.size shouldBe 3
      result should contain(e1)
      result should contain(e2)
      result should contain(e3)

      result should not contain (e4)
    }
  }
}