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
import au.com.onegeek.respite.models.Model
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import reactivemongo.api.ReadPreference
import reactivemongo.bson.{BSONString, BSONDocument}
import spray.caching.{Cache, LruCache}
import uk.gov.hmrc.mongo.Repository

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import au.com.onegeek.respite.controllers.support.LoggingSupport

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
class DatabaseRestActor[ModelType <: Model[ObjectIDType], ObjectIDType](repository: Repository[ModelType, ObjectIDType] with uk.gov.hmrc.mongo.AtomicUpdate[ModelType])
                                                 (implicit val format: Format[ModelType], implicit val stringToId: String => ObjectIDType) extends Actor with LoggingSupport {

  protected implicit def executor: ExecutionContext = ExecutionContext.global

  def doGet(id: String) = {
    logger.debug("Getting something");
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

  def doUpdate(modelInstance: ModelType) = {
    logger.debug("Updating something");
    sender ! updateEntity(modelInstance)
  }

  def doCreate(modelInstance: ModelType) = {
    logger.debug(s"Creating something ${modelInstance}");
    sender ! createEntity(modelInstance)
  }

  def doDeleteEntity(id: String) = {
    logger.debug(s"Deleting something by ID: ${id}");
    sender ! deleteEntity(id)
  }

  def doDeleteEntity(obj: ModelType) = {
    logger.debug(s"Deleting entity: ${obj}");
    sender ! deleteEntity(obj)
  }

  def receive = {
    case "all"                                    => doAll()
    case Seq("get", objectId: String)             => doGet(objectId)
    case Seq("create", modelInstance: ModelType)  => doCreate(modelInstance)
    case Seq("update", modelInstance: ModelType)  => doUpdate(modelInstance)
    case Seq("delete", modelInstance: ModelType)  => doDeleteEntity(modelInstance)
    case Seq("delete", objectID: String)          => doDeleteEntity(objectID)
    case o: Any                                   => logger.debug(s"Received invalid message: $o"); None
  }

  /**
   * Deletes an entity from the755147
   * @param id
   * @return
   */
  def deleteEntity(id: String): Future[Option[ModelType]] = {

    for {
      e <- doGetSingle(id)
      d <- deleteEntityFromDB(id)
    } yield e.orElse(None)

  }

  /**
   * Deletes an entity from the Database
   *
   * @param obj
   * @return
   */
  def deleteEntity(obj: ModelType): Future[Option[ModelType]] = {
    logger.debug(s"Deleting Entity: ${obj}")
    for {
      e <- doGetSingle(obj.id)
      d <- deleteEntityFromDB(obj)
    } yield e.orElse(None)
  }

  def deleteEntityFromDB(obj: ModelType): Future[Option[ModelType]] = {
    repository.removeById(obj.id).map { e => Some(obj) }
  }

  def deleteEntityFromDB(id: String): Future[Option[ModelType]] = {
    logger.debug(s"Deleting Entity by id: ${id}")

    for {
      entity <- doGetSingle(id)
      response <- repository.removeById(id).map { Some(_) }
    } yield entity orElse None

  }

  def createEntity(obj: ModelType): Future[Option[ModelType]] = {
    logger.debug(s"Creating Entity: ${obj}")
    for {
      saved <- repository.insert(obj)
      if saved.ok
    } yield Some(obj)
  }

  // TODO: This is all bad... Not atomicity!
  def updateEntity(obj: ModelType): Future[Option[ModelType]] = {
    logger.debug(s"Updating Entity: ${obj}")
      for {
        e <- doGetSingle(obj.id)
        d <- deleteEntityFromDB(obj)
        saved <- repository.insert(obj)
        if saved.ok
      } yield Some(obj)
  }

  /**
   * List ALL objects
   *
   * @return
   */
  def doList: Future[List[ModelType]] = repository.findAll(ReadPreference.primaryPreferred)

  def doGetSingle(id: String): Future[Option[ModelType]] = {
    logger.info(s"Fetching records by id ${id}")
    repository.findById(id)
  }

  def doGetSingle(id: ObjectIDType): Future[Option[ModelType]] = {
    logger.info(s"Fetching records by id ${id}")
    repository.findById(id)
  }
}