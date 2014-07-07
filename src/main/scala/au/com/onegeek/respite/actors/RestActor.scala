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
package au.com.onegeek.respite.actors

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import au.com.onegeek.respite.DefaultImplicits
import au.com.onegeek.respite.models.Model
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.slf4j.LoggerFactory
import play.api.libs.json._
import reactivemongo.bson.{BSONObjectID, BSONString}
import spray.caching.{Cache, LruCache}
import uk.gov.hmrc.mongo.Repository

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import DefaultImplicits._
import reactivemongo.core.commands.LastError

/**
 * Created by mfellows on 24/04/2014.
 *
 * Bound to a case class of type ModelType, and receives the MongoCollection
 * object that should be used instantiate a data access object (using the
 * com.novus.salat lib), which is in turn used to manage database
 * transactions.
 *
 * Inspects the sender's message and returns a serializable object or List.
 */
class RestActor[ModelType <: Model[BSONObjectID]](repository: Repository[ModelType, BSONObjectID])
                                                 (implicit val bindingModule: BindingModule, implicit val format: Format[ModelType]) extends Actor with Injectable {

  val logger = LoggerFactory.getLogger(getClass)

  val cache: Cache[Option[ModelType]] = LruCache(maxCapacity = 500,
    initialCapacity = 16,
    timeToLive = Duration.Inf,
    timeToIdle = Duration.create(1.0, TimeUnit.MINUTES))

  val listCache: Cache[List[ModelType]] = LruCache(maxCapacity = 500,
    initialCapacity = 16,
    timeToLive = Duration.Inf,
    timeToIdle = Duration.create(1.0, TimeUnit.MINUTES))

  protected implicit def executor: ExecutionContext = ExecutionContext.global

  //  val connection = injectOptional[DefaultDB] getOrElse {
  //    throw new Exception("Database connection not supplied. Death. Ah, horrible horrible.")
  //  }

  def doGet(id: String) = {
    logger.debug("Getting something");

    //    sender ! doGetSingle(objectId).map({ model =>
    //        model match {
    //          case Some(model: ModelType) => {
    //            println(Json.toJson(model))
    //            Json.toJson(model)
    //          }
    //          case _ => NotFound("No object with that id")
    //        }
    //    })

    sender ! doGetSingle(id)
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
    sender ! doList
  }

  def doUpdate(modelInstance: ModelType)(implicit f: String => BSONObjectID) = {
    logger.debug("Updating something");
    sender ! updateEntity(modelInstance)
  }

  def doCreate(modelInstance: ModelType) = {
    logger.debug(s"Creating something ${modelInstance}");
    sender ! createEntity(modelInstance)
  }

  def doDeleteEntity(id: String) = {
    logger.debug(s"Deleting something ${id}");
    sender ! deleteEntity(id)
  }

  def receive = {

    case "all" => doAll()
    case Seq("get", objectId: String, None) => doGet(objectId)
    case Seq("create", modelInstance: ModelType) => println("yo, actor creatin"); doCreate(modelInstance)
    case Seq("update", modelInstance: ModelType) => println("yo, actor updating something "); doUpdate(modelInstance)
    case Seq("delete", objectID: String) => println("yo, actor deleting something"); doDeleteEntity(objectID)
    case _ => println("Ah, NFI what you're askin")
  }


  // TODO: Compose futures together here to delete the entity, purge from cache and return to user...


  /**
   * Deletes an entity from the
   * @param id
   * @return
   */
  def deleteEntity(id: String): Future[Option[ModelType]] = {

    for {
      e <- doGetSingle(id)
      d <- deleteEntityFromDB(id)
      c <- deleteItemFromCache(e)
    } yield e.orElse(None)

  }

  /**
   * TODO: Wouldn't it be nice if I could do this....?
   *
   * @param user The ModelType to remove.
   */
  //  def deleteEntity(user: ModelType): Unit = cache.remove(user) {
  //    deleteEntityFromDB(user)
  //  }

  def deleteEntityFromDB(obj: Option[ModelType]): Future[Option[ModelType]] = {

    obj match {
      case None => Future {
        println("No user to delete: ")
        None
      }
      case Some(obj) => {
        println("Deleting object")
        repository.removeById(obj.id.get).map {
          e =>
            Some(obj)
        }
      }
    }
  }

  def deleteEntityFromDB(id: String)(implicit fo: String => BSONObjectID): Future[Option[LastError]] = {
    println(s"Deleting object ${id}" )
    repository.removeById(id).map { Some(_) }
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

  def createEntity(obj: ModelType): Future[reactivemongo.core.commands.LastError] = {
    repository.insert(obj)
  }

  def updateEntity(obj: ModelType): Future[reactivemongo.core.commands.LastError] = {
    repository.save(obj)
  }

  /**
   * List ALL objects
   *
   * @return
   */
  def doList: Future[List[ModelType]] = listCache("list-users") {
    val list = Await.result(repository.findAll, 100 millis)
    println(list)
    Future {
      list
    }
  }

  //
  def doGetSingle(id: String)(implicit fo: String => BSONObjectID): Future[Option[ModelType]] = cache(id) {

    logger.info(s"Fetching records by id ${id}")
    val foo = Await.result({
      repository.findById(id)
    }, 100 millis)

    println(s"I have me a foo: ${foo}")

    Future {
      foo
    }

  }
}