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

import au.com.onegeek.respite.models._
import com.escalatesoft.subcut.inject.BindingModule
import play.api.libs.json.Json
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.{MongoConnector, ReactiveRepository}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import au.com.onegeek.respite.models.ModelJsonExtensions._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats
import scala.reflect._
import au.com.onegeek.respite.controllers.support.{MetricsRestSupport, MetricsSupport}
import nl.grons.metrics.scala.MetricName
import scala.concurrent.ExecutionContext.Implicits.global

class UserTestRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[User, BSONObjectID]("users", mc.db, modelFormatForMongo {Json.format[User]}, ReactiveMongoFormats.objectIdFormats) {

  override def indexes = Seq(Index(Seq("username" -> IndexType.Ascending), name = Some("keyFieldUniqueIdx"), unique = true, sparse = true))
}
class CatTestRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[Cat, BSONObjectID]("cats", mc.db, modelFormatForMongo {Json.format[Cat]}, ReactiveMongoFormats.objectIdFormats) {

  override def indexes = Seq(Index(Seq("name" -> IndexType.Ascending), name = Some("keyFieldUniqueIdx"), unique = true, sparse = true))
}

// Example of concrete sub-class of RestController
class CatController(repository: ReactiveRepository[Cat, BSONObjectID])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[Cat], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[Cat, BSONObjectID]("cats", Cat.format, repository) {
  // Do stuff, extend me!
}

class MetricSpecController(repository: ReactiveRepository[User, BSONObjectID])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[User], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[User, BSONObjectID]("users", User.format, repository) with MetricsRestSupport[User, BSONObjectID] {
  get("/fooeyfoobar") {
    "foo"
  }

  get("/foo/bar/baz") {
    "foo"
  }

  get("/~") {
    "~"
  }
}

class MetricSpecControllerWithCustomName(repository: ReactiveRepository[User, BSONObjectID])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[User], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[User, BSONObjectID]("users", User.format, repository) with MetricsRestSupport[User, BSONObjectID] {
  override lazy val metricBaseName = {
    MetricName("MyAwesomeName")
  }

}