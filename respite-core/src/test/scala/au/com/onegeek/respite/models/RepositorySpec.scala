package au.com.onegeek.respite.models

import au.com.onegeek.respite.config.TestConfigurationModule
import au.com.onegeek.respite.controllers.UserTestRepository
import au.com.onegeek.respite.test.{Awaiting, MongoSpecSupport}
import org.scalatest.concurrent.ScalaFutures
import au.com.onegeek.respite.UnitSpec
import reactivemongo.bson.BSONObjectID

class RepositorySpec extends UnitSpec with ScalaFutures with MongoSpecSupport with Awaiting {
  implicit val bindingModule = TestConfigurationModule

  val repository = new UserTestRepository

  before {
    repository.removeAll
  }

  "A Repository " should {

    "Serialise BSON Object IDs to a flattened structure" in {

      val key2 = User(id = BSONObjectID("53b62e370100000100af8ece"), username = "bmurray", firstName = "Bill")
      await(repository.insert(key2))

      println("Users in repo: ")
      val users = await(repository.findAll)
      users foreach(u =>
        println(u)
        )
    }

    "De-serialise BSON Object IDs from a flattened structure into a valid structure" in {
    }

    "Allow empty Profile objects" in {
    }

    "reject requests with an invalid API Key" in {

    }
  }
}