import respite.Dependencies
import com.typesafe.sbt.SbtGit._

name := "respite-core"

//libraryDependencies ++= Dependencies.Compile.

//resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
//
//resolvers += "typesafe-snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
//
//
//resolvers += "Akka Repo" at "http://repo.akka.io/repository"
//
//resolvers += "spray repo" at "http://repo.spray.io"
//
//resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

//resolvers += Classpaths.typesafeReleases

parallelExecution in Test := false

libraryDependencies ++= Seq(
  Dependencies.Compile.scalatra,
  Dependencies.Compile.scalate,
  Dependencies.Compile.logback,
  Dependencies.Compile.scalatraAuth,
  Dependencies.Compile.scalatraJson,
  Dependencies.Compile.commonsCodec,
  Dependencies.Compile.sprayCaching,
  Dependencies.Compile.sprayUtil,
  Dependencies.Compile.playJson,
  Dependencies.Compile.ehCacheCore,
  Dependencies.Compile.metrics,
  Dependencies.Compile.simpleMongo,
  Dependencies.Compile.jettyWebapp,
  Dependencies.Compile.jettyServlet,
  Dependencies.Compile.jettyPlus,
  Dependencies.Compile.Test.scalatraTest
)


//libraryDependencies ++= Seq(
//  "org.reactivemongo" %% "reactivemongo" % "0.10.5.akka23-SNAPSHOT"
//  "org.reactivemongo" %% "reactivemongo" % "0.11.0-SNAPSHOT"
//)

//"org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT"

instrumentSettings

ScoverageKeys.minimumCoverage := 80

ScoverageKeys.failOnMinimumCoverage := false

ScoverageKeys.highlighting := true

coverallsSettings

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

// When cross-compiling to 2.11 is possible (downstream library dependency)
//crossScalaVersions := Seq("2.10.4", "2.11.0")
//
//libraryDependencies := {
//  CrossVersion.partialVersion(scalaVersion.value) match {
//    // if scala 2.11+ is used, add dependency on scala-xml module
//    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
//      libraryDependencies.value ++ Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.1")
//    case _ => libraryDependencies.value
//  }
//}

