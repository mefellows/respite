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

import au.com.onegeek.respite.test.MongoSpecSupport
import com.github.simplyscala.MongodProps
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.default.BSONCollection

import scala.reflect._
import scala.util.Failure
import au.com.onegeek.respite.models.AccountComponents._
import com.escalatesoft.subcut.inject.BindingModule
import au.com.onegeek.respite.models.DefaultFormats._
import uk.gov.hmrc.mongo.{ReactiveMongoFormats, ReactiveRepository, MongoConnector}
import reactivemongo.bson.BSONObjectID
import reactivemongo.api.indexes.{IndexType, Index}

class UserTestRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[User, BSONObjectID]("users", mc.db, User.formats, ReactiveMongoFormats.objectIdFormats) {

  override def ensureIndexes() = {
    collection.indexesManager.ensure(Index(Seq("username" -> IndexType.Ascending), name = Some("keyFieldUniqueIdx"), unique = true, sparse = true))
  }
}
class CatTestRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[Cat, BSONObjectID]("cats", mc.db, Cat.formats, ReactiveMongoFormats.objectIdFormats) {

  override def ensureIndexes() = {
    collection.indexesManager.ensure(Index(Seq("name" -> IndexType.Ascending), name = Some("keyFieldUniqueIdx"), unique = true, sparse = true))
  }
}

class UserController(repository: ReactiveRepository[User, BSONObjectID])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[User], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[User, BSONObjectID]("users", User.formats, repository) {
  implicit val t = classTag[User]
}

class CatController(repository: ReactiveRepository[Cat, BSONObjectID])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[Cat], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[Cat, BSONObjectID]("cats", Cat.formats, repository) {
  implicit val t = classTag[Cat]
}

