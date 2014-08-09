package au.com.onegeek.respite.actors

import akka.pattern.ask
import au.com.onegeek.respite.ServletTestsBase
import au.com.onegeek.respite.models.{Cat, User}
import au.com.onegeek.respite.test.{MongoSpecSupport, Awaiting}
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo._
import au.com.onegeek.respite.controllers._
import au.com.onegeek.respite.config.TestConfigurationModule
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}

/**
 * Created by mfellows on 29/06/2014.
 */
class DatabaseRestActorSpec extends ServletTestsBase with ScalaFutures with Awaiting with CurrentTime with MongoSpecSupport {
  implicit val bindingModule = TestConfigurationModule

  import au.com.onegeek.respite.models.ModelJsonExtensions._

  val repository = new UserTestRepository
  val system = ActorSystem()
  implicit val tOut = Timeout(Duration.create(100, MILLISECONDS))
  val actor = system.actorOf(Props(new DatabaseRestActor[User, BSONObjectID](repository)))

  before {
    // Clear out entries - only do this if you don't start/stop between tests
    await(repository.removeAll)

    // Add some keys to test against
    val user1 = User(id = BSONObjectID("53b62e370100000100af8ecd"), username = "mfellows", firstName = "Matt")
    val user2 = User(id = BSONObjectID("53b62e370100000100af8ece"), username = "bmurray", firstName = "Bill")
    await(repository.insert(user1))
    await(repository.insert(user2))

    println("Users in repo: ")
    val users = await(repository.findAll)
    users foreach(u =>
      println(u)
    )
  }

  "A DatabaseRestActor" should {

    "with CRUD methods" should {

      "Provide a 'list' method (CRUD)" in {

      }

      "Provide a 'get' method (CRUD)" in {

      }

      "Provide a 'create' method (CRUD)" in {

      }

      "Provide a 'update' method (CRUD)" in {

      }

      "Provide a 'delete' method by ID (CRUD)" in {

      }

      "Provide a 'delete' method by Object (CRUD)" in {
        val user = User(username = "cat", firstName = "man")
        await(repository.insert(user))

        val resultFuture = Await.result( {actor ? Seq("delete", user)}, 200 millisecond).asInstanceOf[Future[Option[User]]]
        val result = Await.result(resultFuture, 500 millisecond)

        result should equal(Some(user))
        assertUserNotExists("cat")
      }

      "when not given correct input" should {
        "for deleting by id" in {
          val resultFuture = Await.result( {actor ? Seq("delete", "53b62e370100000100af8ecf")}, 200 millisecond).asInstanceOf[Future[Option[User]]]
          val result = Await.result(resultFuture, 500 millisecond)

          result should equal(None)
        }

        "for deleting an object" in {
          val user1 = User(id = BSONObjectID("63b62e370100000100af8ecd"), username = "aoeuaoeu", firstName = "aoeuaoeu")
          val resultFuture = Await.result( {actor ? Seq("delete", user1)}, 200 millisecond).asInstanceOf[Future[Option[User]]]
          val result = Await.result(resultFuture, 500 millisecond)

          result should equal(None)
        }
      }

    }


    "when database is unavailable" should {

      "Return " in {

      }
    }

    "when asked to respond to an unknown query" should {

      "return an Error message" in {
        actor ? "foo"
      }
    }
  }

  /**
   * Confirm a username does not exist in DB.
   *
   * @param username
   */
  def assertUserNotExists(username: String): Unit = {
    val users = await(repository.findAll)

    users foreach(u =>
      u.username shouldNot equal(username)
    )
  }
}