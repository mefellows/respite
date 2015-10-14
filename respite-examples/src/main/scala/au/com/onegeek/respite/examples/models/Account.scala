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
package au.com.onegeek.respite.examples.models

import au.com.onegeek.respite.controllers.RestController
import au.com.onegeek.respite.models.ModelJsonExtensions._
import au.com.onegeek.respite.models.{ApiKey, Model}
import au.com.onegeek.respite.security.{Authentication, ConfigAuthenticationStrategy, AuthServlet, MongoDatabaseAuthServlet}
import com.escalatesoft.subcut.inject.BindingModule
import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext
import scala.reflect._
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository, MongoConnector}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats
import au.com.onegeek.respite.models.ModelJsonExtensions._
import scala.reflect.ClassTag
import au.com.onegeek.respite.controllers.support.{CachingRouteSupport, MetricsSupport, MetricsRestSupport}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by mfellows on 16/07/2014.
 */
case class User(id: BSONObjectID = BSONObjectID.generate, username: String, password: String, firstName: String, lastName: String, dob: DateTime) extends Model[BSONObjectID]
object User { implicit val format = modelFormat { Json.format[User] } }

case class Product(id: BSONObjectID = BSONObjectID.generate, name: String, price: Double) extends Model[BSONObjectID]
object Product { implicit val format = modelFormat { Json.format[Product] } }

case class OrderItem(id: BSONObjectID = BSONObjectID.generate, qty: Int, product: Product) extends Model[BSONObjectID]
object OrderItem { implicit val format = modelFormat { Json.format[OrderItem] } }

case class PromotionCode(id: BSONObjectID = BSONObjectID.generate, code: String, description: String) extends Model[BSONObjectID]
object PromotionCode { implicit val format = modelFormat { Json.format[PromotionCode] } }

case class Order(id: BSONObjectID = BSONObjectID.generate, items: Seq[OrderItem], promo: Option[PromotionCode]) extends Model[BSONObjectID]
object Order { implicit val format = modelFormat { Json.format[Order] } }

case class Account(id: BSONObjectID = BSONObjectID.generate, name: String, orders: Seq[Order], users: Seq[User]) extends Model[BSONObjectID]
object Account { implicit val format = modelFormat { Json.format[Account] } }

// Repositories

class UserRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[User, BSONObjectID]("users", mc.db, modelFormatForMongo {Json.format[User]}, ReactiveMongoFormats.objectIdFormats)
  with uk.gov.hmrc.mongo.AtomicUpdate[User] {

  override def isInsertion(suppliedId: BSONObjectID, returned: User): Boolean = suppliedId.equals(returned.id)

  override def indexes = Seq(
    Index(Seq("username" -> IndexType.Ascending), name = Some("usernameUnq"), unique = true, sparse = true)
  )
}

class ProductRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[Product, BSONObjectID]("products", mc.db, modelFormatForMongo {Json.format[Product]}, ReactiveMongoFormats.objectIdFormats)
  with uk.gov.hmrc.mongo.AtomicUpdate[Product] {

  override def isInsertion(suppliedId: BSONObjectID, returned: Product): Boolean = suppliedId.equals(returned.id)

}

class OrderItemRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[OrderItem, BSONObjectID]("orderItems", mc.db, modelFormatForMongo {Json.format[OrderItem]}, ReactiveMongoFormats.objectIdFormats)
  with uk.gov.hmrc.mongo.AtomicUpdate[Order] {

  override def isInsertion(suppliedId: BSONObjectID, returned: Order): Boolean = suppliedId.equals(returned.id)

}

class PromotionCodeRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[PromotionCode, BSONObjectID]("promotionCodes", mc.db, modelFormatForMongo {Json.format[PromotionCode]}, ReactiveMongoFormats.objectIdFormats)
  with uk.gov.hmrc.mongo.AtomicUpdate[PromotionCode] {

  override def isInsertion(suppliedId: BSONObjectID, returned: PromotionCode): Boolean = suppliedId.equals(returned.id)

}

class OrderRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[Order, BSONObjectID]("orders", mc.db, modelFormatForMongo {Json.format[Order]}, ReactiveMongoFormats.objectIdFormats)
  with uk.gov.hmrc.mongo.AtomicUpdate[Order] {

  override def isInsertion(suppliedId: BSONObjectID, returned: Order): Boolean = suppliedId.equals(returned.id)

}

class AccountRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[Account, BSONObjectID]("accounts", mc.db, modelFormatForMongo {Json.format[Account]}, ReactiveMongoFormats.objectIdFormats)
  with uk.gov.hmrc.mongo.AtomicUpdate[Account] {

  override def isInsertion(suppliedId: BSONObjectID, returned: Account): Boolean = suppliedId.equals(returned.id)

}


// Controllers

// Authentication API with default keys
object ConfigAuthStrategy extends ConfigAuthenticationStrategy {
  override var keys = Map("testkey" -> ApiKey(application = "testapp", description = "Test application", key = "testkey")) ++
                      Map("murray" -> ApiKey(application = "bill", description = "Test application", key = "murray"))
}

class SimpleAuthServlet extends AuthServlet with MetricsSupport {
  override protected implicit def executor: ExecutionContext = ExecutionContext.global
  override implicit val authenticationStrategy = ConfigAuthStrategy
}

// Example of concrete sub-class of RestController

class UserController(repository: ReactiveRepository[User, BSONObjectID] with AtomicUpdate[User])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[User], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[User, BSONObjectID]("users", User.format, repository) with MetricsRestSupport[User, BSONObjectID] with Authentication with CachingRouteSupport {
  override implicit val authenticationStrategy = ConfigAuthStrategy
}

class ProductController(repository: ReactiveRepository[Product, BSONObjectID] with AtomicUpdate[Product])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[Product], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[Product, BSONObjectID]("products", Product.format, repository) with MetricsRestSupport[Product, BSONObjectID] {}

class OrderItemController(repository: ReactiveRepository[OrderItem, BSONObjectID]  with AtomicUpdate[OrderItem])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[OrderItem], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[OrderItem, BSONObjectID]("orderItems", OrderItem.format, repository) {}

class PromotionCodeController(repository: ReactiveRepository[PromotionCode, BSONObjectID] with AtomicUpdate[PromotionCode])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[PromotionCode], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[PromotionCode, BSONObjectID]("promotionCodes", PromotionCode.format, repository) {}

class OrderController(repository: ReactiveRepository[Order, BSONObjectID] with AtomicUpdate[Order])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[Order], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[Order, BSONObjectID]("Orders", Order.format, repository) {}

class AccountController(repository: ReactiveRepository[Account, BSONObjectID] with AtomicUpdate[Account])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[Account], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[Account, BSONObjectID]("Accounts", Account.format, repository) {}