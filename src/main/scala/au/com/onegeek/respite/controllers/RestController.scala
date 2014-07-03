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

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.pattern.ask
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, SECONDS}
import akka.util.Timeout
import org.slf4j.LoggerFactory

import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import au.com.onegeek.respite.actors.RestActor
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import au.com.onegeek.respite.config.TestConfigurationModule
import org.slf4j.Logger
import play.api.libs.json._
import au.com.onegeek.respite.models.AccountComponents.Model
import play.modules.reactivemongo.json.ImplicitBSONHandlers.JsObjectReader
import play.api.libs.json.JsSuccess
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.Some
import reactivemongo.api.DefaultDB
import au.com.onegeek.respite.models.JsonFormats._

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
 * Step 3: Create a generic way of dealing with configuration files (i.e. config.yml in a pre-defined location. https://github.com/dickwall/subcut/blob/master/PropertyFiles.md)
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
class RestController[ObjectType <: Model](collectionName: String)
                                                   (implicit val bindingModule: BindingModule, implicit val reader: Reads[ObjectType], implicit val writer: Writes[ObjectType])
  extends RespiteApiStack with MethodOverride with FutureSupport with Injectable {
  //  val
  val logger = LoggerFactory.getLogger(getClass)
  val system = inject[ActorSystem]
  // getOrElse { throw new Exception("ActorSystem not provided. Death. Ah, horrible horrible.") }
  val connection = injectOptional[DefaultDB] getOrElse {
    throw new Exception("Database connection not supplied. Death. Ah, horrible horrible.")
  }
  protected override implicit val jsonFormats: Formats = DefaultFormats

  val mongoCollection = connection[JSONCollection](collectionName)
  val actor = system.actorOf(Props(new RestActor[ObjectType](mongoCollection)))


  protected implicit def executor: ExecutionContext = system.dispatcher

  implicit val tOut = Timeout(Duration.create(10, SECONDS))

  def doSingle(id: String, method: String, modelInstance: Option[ObjectType] = None) = {
    try {
      //      val objectId = new ObjectId(id.asInstanceOf[String])
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
    logger.debug("Getting all")
    new AsyncResult {
      val is = actor ? "all"
    }
  }

  get("/:id") {
    logger.debug("Getting something")
    val id = params("id")
    doSingle(id, "get")
  }

  post("/") {
    logger.debug("creating something")

    val nameResult: JsResult[ObjectType] = Json.parse(request.body).validate[ObjectType]

    // Move this layer into a Mixin / Trait

    // Then call something like: `model` (instead of parsedbody.extract[ObjectType] -> We've already parsed the body!

    // Validation Errors are already handled at another layer...


    nameResult match {
      case model: JsSuccess[ObjectType] => {
        new AsyncResult {
          logger.debug(s"Received object: ${model.get}")
          val is = actor ? Seq("create", model.get)
        }
      }
      case e: JsError => {
        JsError.toFlatJson(e)
      }
    }
  }

  put("/:id") {
    logger.debug("updating something")
//    val modelInstance = Json.parse(request.body).validate[ObjectType]
//    val id = params("id")
//    doSingle(id, "update", Some(modelInstance))
  }

}