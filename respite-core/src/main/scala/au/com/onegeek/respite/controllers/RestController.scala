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
package au.com.onegeek.respite.controllers

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import au.com.onegeek.respite.actors.DatabaseRestActor
import au.com.onegeek.respite.controllers.support.LoggingSupport
import au.com.onegeek.respite.models.Model
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.scalatra._
import play.api.libs.json._
import uk.gov.hmrc.mongo.{AtomicUpdate, Repository}

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration.{Duration, SECONDS}
import scala.reflect.ClassTag

/*
  Base controller class. Each controller is bound to a case class representing
  a known entity that can be stored in a MongoDB collection and serialized for
  consumers. The controller instance is passed the process' ActorSytem and
  MongoDB connection, both instantiated in ScalatraBootstrap.

  The basic GET collection, GET object, POST, PUT and DELETE routes are
  supported, and child classes can of course implement any additional routes
  as needed.

  Requests are handled asynchronously using akka.Actors. The RestActor receives
  a command corresponding to the HTTP route ("create", "update", etc), and
  whatever objects are required to complete the action - typically a case class
  instance of ObjectType constructed using JSON from the request body,
*/

/**
 * REST API base class with CRUD
 *
 *
 *
 * @param collectionName
 * @param jsonFormatter
 * @param repository
 * @param bindingModule
 * @param tag
 * @param objectIdConverter
 * @tparam ObjectType
 * @tparam ObjectID
 */
class RestController[ObjectType <: Model[ObjectID], ObjectID]
    (val collectionName: String, jsonFormatter: Format[ObjectType], repository: Repository[ObjectType, ObjectID] with AtomicUpdate[ObjectType])
    (implicit val bindingModule: BindingModule, implicit val tag: ClassTag[ObjectType], implicit val objectIdConverter: String => ObjectID)
    extends RespiteApiStack[ObjectType]
    with MethodOverride
    with FutureSupport
    with Injectable
    with LoggingSupport {
  this: LoggingSupport =>

  val system = inject[ActorSystem]
  override implicit val format = jsonFormatter

  val actor = system.actorOf(Props(new DatabaseRestActor[ObjectType, ObjectID](repository)))

  protected implicit def executor: ExecutionContext = ExecutionContext.global

  implicit val tOut = Timeout(Duration.create(1, SECONDS))

  options("/*") {
    response setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
    response setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
  }

  get("/") {
    logger.debug("Getting all")
      new AsyncResult {
        import scala.concurrent.duration._
        override def timeout = tOut.duration
        val is = actor ? "all"
      }
  }

  val getSingle = get("/:id") {
    val id = params("id")
    logger.debug(s"Getting entity by $id")
    new AsyncResult {
      override def timeout = tOut.duration
      val is = actor ? Seq("get", id)
    }
  }

  val createEntity = post("/") {
    logger.debug("creating something")
      val model = getParsedModel[ObjectType].get

    println(s"I have me a model object: ${model}")

      new AsyncResult {
        override def timeout = tOut.duration
        val is = actor ? Seq("create", model)
      }
  }

  val deleteEntity = delete("/:id") {
    val id = params("id")
    logger.debug(s"Deleting something: ${id}")

    new AsyncResult {
      override def timeout = tOut.duration
      val is = actor ? Seq("delete", id)
    }
  }

  val updateEntity = put("/:id") {
    println("PUT called!")
    update
  }

  post("/:id") {
    update
  }

  def update = {
    logger.debug("updating something")

    getParsedModel[ObjectType].map {
      e =>
        println(e)
        new AsyncResult {
          override def timeout = tOut.duration
          val is = actor ? Seq("update", e)
        }
    }.getOrElse {

      // TODO: Still return JS Validation error
      halt(status = 400, reason = "No Request body provided")
    }
  }

  val search = post("/search/") {

  }

  /**
   * Search the current repository by given k/v pairs.
   *
   * Search keys should be in the form: search.key=value&search.key2=value2
   *
   */
//  get("/search/") {
//    val criteria = requestParamsToSearchCriteria(params)
//    logger.debug(criteria.toString)
//
//    new AsyncResult {
//      override def timeout = tOut.duration
//      val is = actor ? Seq("search", criteria)
//    }
//  }
//
//  def requestParamsToSearchCriteria(params: Params): List[(String, JsValue)] = {
////    val searchCriteria: List[Tuple2[String, String]] = params.keys.filter(_.startsWith("search.")) foldLeft(List[Tuple2[String, String]]()) ( (list,k) => (k, params.get(s"search.$k")))
//    params.keys.filter(_.startsWith("search.")).map (_.replaceFirst("search.","")).foldLeft(List[(String, JsValue)]())((list,k) => list.::(k, JsString(params.as[String](s"search.$k"))) )
//  }
}