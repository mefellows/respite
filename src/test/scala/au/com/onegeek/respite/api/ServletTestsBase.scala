package au.com.onegeek.respite.api

import org.scalatest.fixture.FeatureSpec
import org.scalatra.test.scalatest.{ScalatraSuite, ScalatraFlatSpec, ScalatraSpec}
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
abstract class ServletTestsBase extends ScalatraSuite with WordSpecLike with Matchers with BeforeAndAfter