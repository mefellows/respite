//package au.com.onegeek.respite
//
//
//import au.com.onegeek.respite.config.TestConfigurationModule
//import au.com.onegeek.respite.controllers.CatController
//import au.com.onegeek.respite.controllers.CatTestRepository
//import au.com.onegeek.respite.controllers.UserTestRepository
//import au.com.onegeek.respite.models.Cat
//import au.com.onegeek.respite.models.User
//import au.com.onegeek.respite.ServletTestsBase
//import au.com.onegeek.respite.config.TestConfigurationModule
//import au.com.onegeek.respite.models._
//import au.com.onegeek.respite.test.Awaiting
//import au.com.onegeek.respite.test.MongoSpecSupport
//import au.com.onegeek.respite.test.Awaiting
//import org.scalatest.concurrent.ScalaFutures
//import play.api.libs.json.Json
//import reactivemongo.bson.BSONObjectID
//import uk.gov.hmrc.mongo.CurrentTime
//import au.com.onegeek.respite.controllers.support.MetricsSupport
//import au.com.respite.JettyLauncher
//import java.net.ServerSocket
//import org.scalatest._
//import java.io.IOException
//import scala.concurrent.{ExecutionContext, Future}
//
///**
// * Created by mfellows on 22/09/2014.
// */
//class JettyLauncherSpec extends Suite with WordSpecLike with Matchers with BeforeAndAfter {
//  lazy val socket = new ServerSocket(8001)
//  var launcher: JettyLauncherTest = null
//
//  protected implicit def executor: ExecutionContext = ExecutionContext.global
//
//  class JettyLauncherTest extends JettyLauncher {
//    override lazy val port = 8001
//
//    def shutdown: Unit = {
//      server.setStopAtShutdown(true)
//      server.setStopTimeout(1000)
//    }
//  }
//
//  before {
//    Future {
//      println("Running Jetty Launcher in background...")
//      launcher = new JettyLauncherTest
//      launcher.main(Array())
//    }
//  }
//
//  after {
//    launcher.shutdown
//  }
//
//  "A JettyLauncher" should {
//
//    "Main" in {
//
//      // Sorry, I hate arbitrary sleeps too...
//      Thread.sleep(2000);
//      intercept[IOException] {
//        socket.getLocalPort
//      }
//    }
//
//  }
//}