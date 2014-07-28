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
import au.com.onegeek.respite.controllers.support.{MetricsSupport, LoggingSupport}
import au.com.onegeek.respite.models.Model
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.scalatra._
import play.api.libs.json._
import uk.gov.hmrc.mongo.Repository

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, SECONDS}
import scala.reflect.ClassTag


/**
 * Created by mfellows on 24/04/2014.
 */


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


/*
 * Proposed approach:
 *
 * Step 1: Create a generic CRUD Rest Controller (which is basically what the below does)
 * Step 2.1: Create a type class system that models the main objects required - e.g. a data store representation etc. that can be swapped out
 * Step 2.2: Design any annotations/types required to allow for persistance in the model e.g. @Model or with 'Model' type class
 * Step 3: Create a generic way of dealing with configuration files -> Environment files. Create a `Scalaman` project as a Foreman equivalent?
 * Step 4: Remove the requirement of 'Angular' in the framework and replace with Akka etc.
 * Step 5: Create the default folder 'structure' and 'the Scalam way' of doing things - be opinionated but still allow for customisation
 *
 *
 * Use Stackable Traits / Features (similar to Skinny) to allow for customisation of behaviour.
 * USe type system where possible (do annotations use Type system or not? Assume not)
 * Remove try/catch. *shudders*
 *
 * End: Create/update yeoman generator to get started quickly, and allow for commands to add new models, services and so on
 */

// TODO: Consider making RestController an abstract/Trait and creating specific, concrete implementations (Reactive, Postgres... versions)
class RestController[ObjectType <: Model[ObjectID], ObjectID]
    (collectionName: String, jsonFormatter: Format[ObjectType], repository: Repository[ObjectType, ObjectID])
    (implicit val bindingModule: BindingModule, implicit val tag: ClassTag[ObjectType], implicit val objectIdConverter: String => ObjectID)
//    (implicit val bindingModule: BindingModule, implicit val tag: ClassTag[ObjectType])
    extends RespiteApiStack[ObjectType]
    with MethodOverride
    with FutureSupport
    with MetricsSupport
    with Injectable
    with LoggingSupport { this: LoggingSupport =>

  // Metrics
  private[this] val loading = metrics.timer(s"api-$collectionName-loading")
  private[this] val counters = metrics.counter(s"api-$collectionName-counters")

  // Would like to compose this metrics into a single function call...
  //private[this] val all = loading compose counters

  // Health Checks
  healthCheck("get", unhealthyMessage = s"GET $collectionName service not available") {
    true
  }

  val system = inject[ActorSystem]
  override implicit val format = jsonFormatter


  val actor = system.actorOf(Props(new DatabaseRestActor[ObjectType, ObjectID](repository)))

  protected implicit def executor: ExecutionContext = system.dispatcher
//  protected implicit def objectIdConverter: BSONObjectID => String


  implicit val tOut = Timeout(Duration.create(10, SECONDS))

  def doSingle(id: String, method: String, modelInstance: Option[ObjectType] = None) = {
    try {
      new AsyncResult {
        val is = actor ? Seq(method, id, modelInstance)
      }
    }
    catch {
      case e: Exception => {
        logger.error("Something died: " + e.getMessage)
        BadRequest("You probably have a malformed id: " + e.getMessage)
      }
    }
  }

  get("/") {
    loading.time {
      counters += 1
      logger.debug("Getting all")
      new AsyncResult {
        val is = actor ? "all"
      }
    }
  }

  get("/:id") {
    logger.debug("Getting something")
    val id = params("id")
    doSingle(id, "get")
  }

  post("/") {
    logger.debug("creating something")
      val model = getParsedModel[ObjectType].get

    println(s"I have me a model object: ${model}")

      new AsyncResult {
        val is = actor ? Seq("create", model)
      }
  }

  delete("/:id") {
    val id = params("id")
    logger.debug(s"Deleting something: ${id}")

    new AsyncResult {
      val is = actor ? Seq("delete", id)
    }
    //    val modelInstance = Json.parse(request.body).validate[ObjectType]
    //    val id = params("id")
    //    doSingle(id, "update", Some(modelInstance))
  }

  put("/:id") {
    logger.debug("updating something")

    getParsedModel[ObjectType].map { e =>
      println(e)
      new AsyncResult {
        val is = actor ? Seq("update", e)
      }
    }.getOrElse {

      // TODO: Still return JS Validation error
      halt(status = 400, reason = "No Request body provided")
    }
  }


  post("/search/") {

  }

  /**
   * Search the current repository by given k/v pairs.
   *
   * Search keys should be in the form: search.key=value&search.key2=value2
   *
   */
  get("/search/") {
    val criteria = requestParamsToSearchCriteria(params)
    logger.debug(criteria.toString)

    new AsyncResult {
      val is = actor ? Seq("search", criteria)
    }
  }

  def requestParamsToSearchCriteria(params: Params): List[(String, JsValue)] = {
//    val searchCriteria: List[Tuple2[String, String]] = params.keys.filter(_.startsWith("search.")) foldLeft(List[Tuple2[String, String]]()) ( (list,k) => (k, params.get(s"search.$k")))
    params.keys.filter(_.startsWith("search.")).map (_.replaceFirst("search.","")).foldLeft(List[(String, JsValue)]())((list,k) => list.::(k, JsString(params.as[String](s"search.$k"))) )
  }
}