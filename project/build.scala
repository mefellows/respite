import sbt.Keys._
import sbt._

object RespiteBuild extends Build {
  val Organization = "au.com.onegeek"
  val Name = "Respite REST Framework"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.3"
  val ScalatraVersion = "2.2.2"

//  import com.typesafe.sbt.SbtStartScript

//  seq(SbtStartScript.startScriptForClassesSettings: _*)

  lazy val project = Project (
    "respite",
    file("."),
//    settings = Seq(com.typesafe.sbt.SbtStartScript.startScriptForClassesSettings: _*) ++ Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
    settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
      resolvers += "Akka Repo" at "http://repo.akka.io/repository",
      resolvers += "spray repo" at "http://repo.spray.io",
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
        "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test",
        "org.scalatra" %% "scalatra-scalatest" % "2.2.2" % "test",
        "org.reactivemongo" %% "reactivemongo" % "0.10.0",
        "org.scalatra" %% "scalatra-auth" % ScalatraVersion,
        "org.json4s"   %% "json4s-jackson" % "3.1.0",
        "commons-codec" % "commons-codec" % "1.2",
        "org.scalatra" %% "scalatra-json" % "2.2.2",
        "com.escalatesoft.subcut" %% "subcut" % "2.0",
        "io.spray" % "spray-caching" % "1.2.1",
        "io.spray" % "spray-util" % "1.2.1",
        "com.typesafe.play" %% "play-json" % "2.2.3",
        "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
        "net.sf.ehcache" % "ehcache-core" % "2.6.8",
        "uk.gov.hmrc" %% "simple-reactivemongo" % "1.1.0",
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.10.v20130312" % "compile",
        "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.1" % "test",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "compile;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
      )
    )
  )
}