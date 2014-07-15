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
import reactivemongo.bson.BSONObjectID
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
//class ReactiveMongoRestController[ObjectType <: Model[BSONObjectID]] extends RestController(collectionName: String, jsonFormatter: Format[ObjectType], repository: Repository[ObjectType, BSONObjectID])
//(implicit val bindingModule: BindingModule, implicit val tag: ClassTag[ObjectType])
//  with RespiteApiStack[ObjectType]
//  with MethodOverride
//  with FutureSupport
//  with Injectable
//  with LoggingSupport {
//  this: LoggingSupport =>
//
//    val system = inject[ActorSystem]
//
//    implicit val format = jsonFormatter
//
//    implicit def StringToBSONObjectId(s: String): BSONObjectID = BSONObjectID(s)
//    implicit def BSONObjectIdToString(s: BSONObjectID): String = s.stringify
//
//    val actor = system.actorOf(Props(new ReactiveMongoDatabaseRestActor[ObjectType, BSONObjectID](repository)))
//}

//class ReactiveMongoRestController[ObjectType <: Model[ObjectID], ObjectID](collectionName: String, jsonFormatter: Format[ObjectType], repository: Repository[ObjectType, ObjectID]) extends RestController[ObjectType, ObjectID](collectionName: String, jsonFormatter: Format[ObjectType], repository: Repository[ObjectType, ObjectID]) {
//class ReactiveMongoRestController[ObjectType <: Model[BSONObjectID]](collectionName: String, jsonFormatter: Format[ObjectType], repository: Repository[ObjectType, BSONObjectID])(override val bindingModule: BindingModule, override val tag: ClassTag[ObjectType]) extends RestController(collectionName, jsonFormatter, repository)(bindingModule, tag) {
//  override protected val actor = system.actorOf(Props(new ReactiveMongoDatabaseRestActor[ObjectType, BSONObjectID](repository)))
//
//  implicit def StringToBSONObjectId(s: String): BSONObjectID = BSONObjectID(s)
//  implicit def BSONObjectIdToString(s: BSONObjectID): String = s.stringify
//
//  //  protected implicit def objectIdConverter: BSONObjectID => String
//  override protected implicit def objectIdConverter: (String) => BSONObjectID = StringToBSONObjectId
//}