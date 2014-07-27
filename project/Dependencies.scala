package respite

import sbt.Keys._
import sbt._

object Dependencies {

  object Versions {
    val ScalaVersion = "2.10.3"
    val ScalatraVersion = "2.2.2"
  }

  object Compile {

    import Versions._

    val scalatra         = "org.scalatra"              %% "scalatra"                 % ScalatraVersion
    val scalatraJson     = "org.scalatra"              %% "scalatra-json"            % "2.2.2"
    val scalatraAuth     = "org.scalatra"              %% "scalatra-auth"            % ScalatraVersion
    val scalate          = "org.scalatra"              %% "scalatra-scalate"         % ScalatraVersion
    val specs2           = "org.scalatra"              %% "scalatra-specs2"          % ScalatraVersion             % "test"
    val logback          = "ch.qos.logback"            %  "logback-classic"          % "1.0.6"                     % "runtime"
    val reactiveMongo    = "org.reactivemongo"         %% "reactivemongo"            % "0.10.0"
    val jackson4s        = "org.json4s"                %% "json4s-jackson"           % "3.1.0"
    val commonsCodec     = "commons-codec"             %  "commons-codec"            % "1.2"
    val subcut           = "com.escalatesoft.subcut"   %% "subcut"                   % "2.0"
    val sprayCaching     = "io.spray"                  %  "spray-caching"            % "1.2.1"
    val sprayUtil        = "io.spray"                  %  "spray-util"               % "1.2.1"
    val playJson         = "com.typesafe.play"         %% "play-json"                % "2.2.3"
    val playMongo        = "org.reactivemongo"         %% "play2-reactivemongo"      % "0.10.2"
    val simpleMongo      = "uk.gov.hmrc"               %% "simple-reactivemongo"     % "1.1.0"
    val ehCacheCore      = "net.sf.ehcache"            %  "ehcache-core"             % "2.6.8"
    val jettyWebapp      = "org.eclipse.jetty"         %  "jetty-webapp"             % "8.1.10.v20130312"          % "compile"
    val jettyServlet     = "org.eclipse.jetty.orbit"   %  "javax.servlet"            % "3.0.0.v201112011016"       % "compile;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))

    // Web
    object Web {
      val jettyWebapp      = "org.eclipse.jetty"         %  "jetty-webapp"             % "8.1.10.v20130312"          % "container;compile"
      val jettyServlet     = "org.eclipse.jetty.orbit"   %  "javax.servlet"            % "3.0.0.v201112011016"       % "container;compile;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
    }
    // Test

    object Test {
      val scalatest        = "org.scalatest"             %  "scalatest_2.10"           % "2.1.0"                     % "test"
      val scalatraTest     = "org.scalatra"              %% "scalatra-scalatest"       % "2.2.2"                     % "test"
      val scalatestMongo   = "com.github.simplyscala"    %% "scalatest-embedmongo"     % "0.2.1"                     % "test"
    }

  }
}