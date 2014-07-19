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
package au.com.onegeek.respite.models

import play.api.libs.json.JsValue
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.LastError
import uk.gov.hmrc.mongo.{DatabaseUpdate, CurrentTime, ReactiveRepository}

import scala.concurrent.{Future, ExecutionContext}

/**
 * The Result of the last database command.
 *
 * @param ok True if the last operation was successful
 * @param err The err field, if any
 * @param code The error code, if any
 * @param errMsg The message (often regarding an error) if any
 * @param originalDocument The whole map resulting of the deserialization of the response with the [[reactivemongo.bson.DefaultBSONHandlers DefaultBSONHandlers]].
 * @param updated The number of documents affected by last command, 0 if none
 * @param updatedExisting When true, the last update operation resulted in change of existing documents
 */
case class Result (ok: Boolean, err: Option[String], code: Option[Int], errMsg: Option[String], originalDocument: Option[BSONDocument], updated: Int, updatedExisting: Boolean)

/**
 * Canonical representation of a database Repository. Not tied with any specific implementation.
 *
 * This will be a wrapper for something like `ReactiveRepository` that can implement Mongo-specific behaviour.
 *
 * Created by mfellows on 30/06/2014.
 */
trait RespiteRepository[A <: Any, ID <: Any] extends CurrentTime {

    def findAll(implicit ec: ExecutionContext): Future[List[A]] = ???

    def findById(id: ID)(implicit ec: ExecutionContext): Future[Option[A]] = ???

    def find(query: (scala.Predef.String, play.api.libs.json.Json.JsValueWrapper)*)(implicit ec: ExecutionContext): Future[List[A]] = ???

    def find(query: JsValue)(implicit ec: ExecutionContext): Future[List[A]] = ???

    def count(implicit ec: ExecutionContext): Future[Int] = ???

    def removeAll(implicit ec: ExecutionContext): Future[LastError] = ???

    def removeById(id: ID)(implicit ec: ExecutionContext): Future[LastError] = ???

    def remove(query: (scala.Predef.String, play.api.libs.json.Json.JsValueWrapper)*)(implicit ec: ExecutionContext): Future[LastError] = ???

    def drop(implicit ec: ExecutionContext): Future[Boolean] = ???

    def save(entity: A)(implicit ec: ExecutionContext): Future[LastError] = ???

    def insert(entity: A)(implicit ec: ExecutionContext): Future[LastError] = ???

    def saveOrUpdate(findQuery: => Future[Option[A]], ifNotFound: => Future[A], modifiers: (A) => A)(implicit ec: ExecutionContext): Future[DatabaseUpdate[A]] = ???
}

trait PageableRepository {

  // Decorates a `Repository` instance with pagination data
}