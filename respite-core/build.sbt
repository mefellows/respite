import bintray.Keys._
import respite.Dependencies
import com.typesafe.sbt.SbtGit._

name := "respite-core"

//libraryDependencies ++= Dependencies.Compile.

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Akka Repo" at "http://repo.akka.io/repository"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += Classpaths.typesafeReleases

parallelExecution in Test := false

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
  Dependencies.Compile.ehCacheCore,
  Dependencies.Compile.simpleMongo,
  Dependencies.Compile.jettyServlet,
  Dependencies.Compile.jettyWebapp,
  Dependencies.Compile.Test.scalatest,
  Dependencies.Compile.Test.scalatestMongo,
  Dependencies.Compile.Test.scalatraTest
)

instrumentSettings

ScoverageKeys.minimumCoverage := 80

ScoverageKeys.failOnMinimumCoverage := false

ScoverageKeys.highlighting := true

coverallsSettings

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))