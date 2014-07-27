package au.com.onegeek.respite.models

import au.com.onegeek.respite.config.TestConfigurationModule
import org.scalatest.concurrent.ScalaFutures
import au.com.onegeek.respite.UnitSpec


class JsonFormatsSpec extends UnitSpec with ScalaFutures {
  implicit val bindingModule = TestConfigurationModule

  "The default JSON Formats" should {

    "Serialise BSON Object IDs to a flattened structure" in {
    }

    "De-serialise BSON Object IDs from a flattened structure into a valid structure" in {
    }

    "Allow empty Profile objects" in {
    }

    "reject requests with an invalid API Key" in {

    }
  }


}