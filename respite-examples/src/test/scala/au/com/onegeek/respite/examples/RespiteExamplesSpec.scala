package au.com.onegeek.respite.examples

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class RespiteExamplesSpec extends ScalatraSpec { def is =
  "GET / on RespiteExamples"                     ^
    "should return status 200"                  ! root200^
                                                end

  addServlet(classOf[RespiteExamples], "/*")

  def root200 = get("/") {
    status must_== 200
  }
}
