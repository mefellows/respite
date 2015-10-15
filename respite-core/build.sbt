import java.io.File
import respite.Dependencies
import com.typesafe.sbt.SbtGit._
import org.scoverage.coveralls.Imports.CoverallsKeys._

name := "respite-core"

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Akka Repo" at "http://repo.akka.io/repository"

resolvers += "spray repo" at "http://repo.spray.io"

// This seems to be slower than sonatype??
// resolvers += Resolver.jcenterRepo

resolvers += Resolver.bintrayRepo("hmrc", "releases")

resolvers += Resolver.bintrayRepo("mefellows", "mfellows")

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  Dependencies.Compile.logback,
  Dependencies.Compile.scalatra,
  Dependencies.Compile.scalate,
  Dependencies.Compile.specs2,
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
  Dependencies.Compile.Test.pegdown,
  Dependencies.Compile.Test.scalatraTest,
  Dependencies.Compile.Test.scalaMock
)

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 80

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := false

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

// GitHub Pages, API Docu and Test Specs

site.settings

scalacOptions += "-target:jvm-1.8"

lazy val specFile = taskKey[File]("File path to test output")

specFile in Test := file("target/test-reports")

mappings in Test := Seq(file("target/test-reports") -> "latest/specifications")

site.addMappingsToSiteDir(mappings in(Test, specFile), "latest/specifications")

ghpages.settings

git.remoteRepo := "git@github.com:mefellows/respite.git"

site.includeScaladoc()

site.jekyllSupport()

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports")

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDSI")