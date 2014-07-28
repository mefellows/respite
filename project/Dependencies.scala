package respite

import sbt.Keys._
import sbt._

object Dependencies {

  object Versions {
    val ScalaVersion = "2.10.3"
    val ScalatraVersion = "2.2.2"
//    val ScalatraVersion = "2.3.0"
    //val jettyVersion = "9.1.3.v20140225"
    val jettyVersion = "8.1.10.v20130312"
  }

  object Compile {

    import Versions._

    val scalatra         = "org.scalatra"                 %% "scalatra"                 % ScalatraVersion
    val scalatraJson     = "org.scalatra"                 %% "scalatra-json"            % ScalatraVersion
    val scalatraAuth     = "org.scalatra"                 %% "scalatra-auth"            % ScalatraVersion
    val scalate          = "org.scalatra"                 %% "scalatra-scalate"         % ScalatraVersion
    val logback          = "ch.qos.logback"               %  "logback-classic"          % "1.0.6"                     % "runtime"
    val reactiveMongo    = "org.reactivemongo"            %% "reactivemongo"            % "0.10.0"
    val playMongo        = "org.reactivemongo"            %% "play2-reactivemongo"      % "0.10.2"
    val simpleMongo      = "uk.gov.hmrc"                  %% "simple-reactivemongo"     % "1.1.0"
//    val jackson4s        = "org.json4s"                   %% "json4s-jackson"           % "3.1.0"
//    val commonsCodec     = "commons-codec"                %  "commons-codec"            % "1.2"
    val subcut           = "com.escalatesoft.subcut"      %% "subcut"                   % "2.0"
    val sprayCaching     = "io.spray"                     %  "spray-caching"            % "1.2.1"
    val sprayUtil        = "io.spray"                     %  "spray-util"               % "1.2.1"
    // Provided by Simple reactive mongo
//    val playJson         = "com.typesafe.play"            %% "play-json"                % "2.2.3"
    val metrics          = "nl.grons"                     %% "metrics-scala"            % "3.2.0_a2.3"
//    val ehCacheCore      = "net.sf.ehcache"               %  "ehcache-core"             % "2.6.8"
//    val jettyServlet     = "javax.servlet"                % "javax.servlet-api"         % "3.1.0"                     % "compile;provided;test" artifacts Artifact("javax.servlet-api", "jar", "jar")
    val jettyServlet     = "org.eclipse.jetty.orbit"      % "javax.servlet"             % "3.0.0.v201112011016"       % "compile;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
    val jettyWebapp      = "org.eclipse.jetty"            % "jetty-webapp"              % jettyVersion
    val jettyPlus        = "org.eclipse.jetty"            % "jetty-plus"                % jettyVersion
    val jettySocket      = "org.eclipse.jetty.websocket"  %  "websocket-server"         % jettyVersion                % "container;provided"

    // Web
    object Web {
      //val jettyServlet     = "javax.servlet"                % "javax.servlet-api"         % "3.1.0"                % "container;compile;provided;test" artifacts Artifact("javax.servlet-api", "jar", "jar")
      val jettyServlet     = "org.eclipse.jetty.orbit"      % "javax.servlet"             % "3.0.0.v201112011016"  % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
      val jettyWebapp      = "org.eclipse.jetty"            % "jetty-webapp"              % jettyVersion           % "container"
      val jettyPlus        = "org.eclipse.jetty"            % "jetty-plus"                % jettyVersion           % "container;provided"
      val jettySocket      = "org.eclipse.jetty.websocket"  %  "websocket-server"         % jettyVersion           % "container;provided"
    }
    // Test

    object Test {
      val scalaTest        = "org.scalatest"             %% "scalatest"                % "2.2.0"                     % "test"
      val scalatraTest     = "org.scalatra"              %% "scalatra-scalatest"       % ScalatraVersion             % "test"
//      val scalatestMongo   = "com.github.simplyscala"    %% "scalatest-embedmongo"     % "0.2.1"                     % "test"
    }

  }
}
