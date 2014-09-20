import respite.Dependencies
import com.typesafe.sbt.SbtGit._

name := "respite-core"

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Akka Repo" at "http://repo.akka.io/repository"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  Dependencies.Compile.scalatra,
  Dependencies.Compile.scalate,
  Dependencies.Compile.specs2,
  Dependencies.Compile.logback,
  Dependencies.Compile.reactiveMongo,
  Dependencies.Compile.scalatraAuth,
  Dependencies.Compile.scalatraJson,
  Dependencies.Compile.jackson4s,
  Dependencies.Compile.commonsCodec,
  Dependencies.Compile.subcut,
  Dependencies.Compile.sprayCaching,
  Dependencies.Compile.sprayUtil,
  Dependencies.Compile.playJson,
  Dependencies.Compile.playMongo,
  Dependencies.Compile.metrics,
  Dependencies.Compile.metricsAdmin,
  Dependencies.Compile.ehCacheCore,
  Dependencies.Compile.simpleMongo,
  Dependencies.Compile.jettyServlet,
  Dependencies.Compile.jettyWebapp,
  Dependencies.Compile.Test.scalatest,
  Dependencies.Compile.Test.pegdown,
  Dependencies.Compile.Test.scalatestMongo,
  Dependencies.Compile.Test.scalatraTest,
  Dependencies.Compile.Test.scalaMock,
  Dependencies.Compile.Test.mockito
)

instrumentSettings

ScoverageKeys.minimumCoverage := 95

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.highlighting := true

coverallsSettings

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

// GitHub Pages, API Docu and Test Specs

site.settings

ghpages.settings

git.remoteRepo := "git@github.com:mefellows/respite.git"

site.includeScaladoc()

site.jekyllSupport()

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports")

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDSI")