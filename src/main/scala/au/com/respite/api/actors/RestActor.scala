package au.com.respite.api.actors

import akka.actor.{Actor, ActorSystem}
import org.scalatra.{NotFound, BadRequest}
import reactivemongo.api.collections.default.BSONCollection
import org.slf4j.LoggerFactory
import au.com.respite.api.models.DAOMappers._
import au.com.respite.api.models.AccountComponents._
import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import spray.caching.{LruCache, Cache}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats._
import scala.util.Failure
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.Some
import reactivemongo.api.DefaultDB

/**
 * Created by mfellows on 24/04/2014.
 *
 * Bound to a case class of type ObjectType, and receives the MongoCollection
 * object that should be used instantiate a data access object (using the
 * com.novus.salat lib), which is in turn used to manage database
 * transactions.
 *
 * Inspects the sender's message and returns a serializable object or List.
 */
class RestActor[ObjectType <: AnyRef](collection: JSONCollection)
                                     (implicit val bindingModule: BindingModule, implicit val reader: Reads[ObjectType], implicit val writer: Writes[ObjectType]) extends Actor with Injectable {

  val logger =  LoggerFactory.getLogger(getClass)

  val cache: Cache[Option[ObjectType]] = LruCache(maxCapacity = 500,
    initialCapacity = 16,
    timeToLive = Duration.Inf,
    timeToIdle = Duration.create(1.0, TimeUnit.MINUTES))

  val listCache: Cache[List[ObjectType]] = LruCache(maxCapacity = 500,
    initialCapacity = 16,
    timeToLive = Duration.Inf,
    timeToIdle = Duration.create(1.0, TimeUnit.MINUTES))

  protected implicit def executor: ExecutionContext = ExecutionContext.global

  val connection = injectOptional[DefaultDB] getOrElse {
    throw new Exception("Database connection not supplied. Death. Ah, horrible horrible.")
  }

  def doGet(objectId: String) = {
    logger.debug("Getting something");

    sender ! doGetSingle(objectId).map({ model =>
        model match {
          case Some(model: ObjectType) => {
            println(Json.toJson(model))
            Json.toJson(model)
          }
          case _ => NotFound("No object with that id")
        }
    })
  }

  /**
   * List ALL objects
   *
   * TODO: Consider use of BSONDocuments instead of the case classes when dealing with DB Interactions.
   * Either that, or look at reflection / Manifest. Possibly even both.
   *
   * What's the performance impact of Reflection vs Reactive DB connections I wonder?
   *
   * @return
   */
  def doAll() = {
    logger.debug("Getting all");

    val list = doList
    val jsonList = list.map { obj =>
      Json.toJson(obj)
    }
    sender ! jsonList
  }

  def doUpdate(objectId: String, modelInstance: ObjectType) = {
    logger.debug("Updating something");
  }

  def doCreate(modelInstance: ObjectType) = {
    logger.debug(s"Creating something ${modelInstance}");
    sender ! createEntity(modelInstance)
  }

  def receive = {
    case "all" => doAll()
    case Seq("get", objectId: String, None) => doGet(objectId)
    case Seq("update", objectId: String, Some(modelInstance: ObjectType)) =>
      doUpdate(objectId, modelInstance)
    case Seq("create", modelInstance: ObjectType) => doCreate(modelInstance)
    case _ =>
  }


  // TODO: Compose futures together here to delete the entity, purge from cache and return to user...

  /**
   * Deletes an entity from the
   * @param id
   * @return
   */
  def deleteEntity(id: String): Future[Option[ObjectType]] = {

    for {
      e <- doGetSingle(id)
      d <- deleteEntityFromDB(e)
      c <- deleteItemFromCache(e)
    } yield e.orElse(None)

  }

  /**
   * TODO: Wouldn't it be nice if I could do this....?
   *
   * @param user The ObjectType to remove.
   */
  //  def deleteEntity(user: ObjectType): Unit = cache.remove(user) {
  //    deleteEntityFromDB(user)
  //  }

  def deleteEntityFromDB(obj: Option[ObjectType]): Future[Option[ObjectType]] = {

    obj match {
      case None => Future {
        println("No user to delete: ")
        None
      }
      case Some(user) => {
        println("Deleting object")
        collection.remove(obj).map { e =>
            Some(user)
        }
      }
    }
  }

  def deleteItemFromCache(key: Option[Any]): Future[Option[Any]] = {
    Future {
      key match {
        case None => None
        case Some(key) => {
          cache.remove(key)
          Some(key)
        }
      }
    }
  }

  def createEntity(obj: ObjectType): Future[reactivemongo.core.commands.LastError] = {
    collection.insert(obj)
  }

  /**
   * List ALL objects
   *
   * @return
   */
  def doList: Future[List[ObjectType]] = listCache("list-users") {
    val query = BSONDocument()
    val cursor = collection.find(query).cursor[ObjectType].collect[List](25)
    cursor
  }

  //
  def doGetSingle(id: String): Future[Option[ObjectType]] = cache(id) {
    logger.info("Fetching records!")
    val query = BSONDocument("_id" -> BSONObjectID(id))

    collection.
      find(query).
      one[ObjectType]
  }
}