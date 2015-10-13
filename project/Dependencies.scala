package respite

import sbt.Keys._
import sbt._

object Dependencies {

  object Versions {
    val ScalaVersion = "2.11.7"
//    val ScalaVersion = "2.10.6"
    val ScalatraVersion = "2.3.1"
  }

  object Compile {

    import Versions._

    val scalatra         = "org.scalatra"              %% "scalatra"                 % ScalatraVersion
    val scalatraJson     = "org.scalatra"              %% "scalatra-json"            % ScalatraVersion
    val scalatraAuth     = "org.scalatra"              %% "scalatra-auth"            % ScalatraVersion
    val scalate          = "org.scalatra"              %% "scalatra-scalate"         % ScalatraVersion
    val specs2           = "org.scalatra"              %% "scalatra-specs2"          % ScalatraVersion             % "test"
    val logback          = "ch.qos.logback"            % "logback-classic"           % "1.1.3"                     % "runtime"

//    val reactiveMongo    = "org.reactivemongo"         %% "reactivemongo"            % "0.11.7"
    val playMongoDep     = "org.reactivemongo"         %% "play2-reactivemongo"      % "0.10.5.0.akka23"
    val playMongo        = playMongoDep.exclude        ("org.apache.logging.log4j",    "log4j-to-slf4j")
    val jackson4s        = "org.json4s"                %% "json4s-jackson"           % "3.2.11"
    val commonsCodec     = "commons-codec"             %  "commons-codec"            % "1.2"
    val subcut           = "com.escalatesoft.subcut"   %% "subcut"                   % "2.1"
    val sprayCaching     = "io.spray"                  %%  "spray-caching"            % "1.3.3"
    val sprayUtil        = "io.spray"                  %%  "spray-util"               % "1.3.3"
//    val playJson         = "com.typesafe.play"         %% "play-json"                % "2.4.3"
    val playJson         = "com.typesafe.play"         %% "play-json"                % "2.3.10"
    val metrics          = "nl.grons"                  %% "metrics-scala"            % "3.5.2_a2.3"
    val metricsAdmin     = "io.dropwizard.metrics"     %  "metrics-servlets"          % "3.1.2"
     // val simpleMongo      = "uk.gov.hmrc"               %% "simple-reactivemongo"     % "3.0.1"
    // val simpleMongo      = "uk.gov.hmrc"              %% "simple-reactivemongo"      % "3.0.1-1-g4c93457"
   val simpleMongo      = "uk.gov.hmrc"              %% "simple-reactivemongo"      % "4.2.1-0-g0000000"
//    https://bintray.com/artifact/download/mefellows/mfellows/uk/gov/hmrc/simple-reactivemongo_2.11/4.2.1-0-g0000000/simple-reactivemongo_2.11-4.2.1-0-g0000000.jar
    val ehCacheCore      = "net.sf.ehcache"            %  "ehcache-core"             % "2.6.8"
//    val jettyWebapp      = "org.eclipse.jetty"         %  "jetty-webapp"             % "9.2.10.v20150310"          % "compile"
//    val jettyWebrapp        = "org.eclipse.jetty"       % "jetty-webapp"              % "8.1.8.v20121106"           % "compile"
//    val jettyServlet     = "org.eclipse.jetty.orbit"   %  "javax.servlet"            % "3.0.0.v201112011016"       % "compile;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
//    val jettyWebapp      = "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310"
//    val jettyServlet     = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "compile;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
    val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310"
    val jettyServlet = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

    // Web
    object Web {
//      val jettyWebapp        = "org.eclipse.jetty"       % "jetty-webapp"              % "8.1.8.v20121106"           % "compile"
//      val jettyServlet     = "org.eclipse.jetty.orbit"   %  "javax.servlet"            % "3.0.0.v201112011016"       % "container;compile;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
//      val jettyWebapp      = "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310" % "container"
//      val jettyServlet     = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;compile;provided;test"  artifacts (Artifact("javax.servlet", "jar", "jar"))

      val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310" % "container"
      val jettyServlet = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
    }
    // Test

    object Test {
//      val scalatest        = "org.scalatest"           %%  "scalatest"                  % "2.3.4"                     % "test"
      val pegdown          = "org.pegdown"             %  "pegdown"                     % "1.0.2"                     // Need this lib to remove error: java.lang.NoClassDefFoundError: org/pegdown/PegDownProcessor
      val scalatraTest     = "org.scalatra"            %% "scalatra-scalatest"          % ScalatraVersion             % "test" //exclude("org.scalatra", "scalatra-scalatest_2.10")
//      val scalatestMongo   = "com.github.simplyscala"  % "scalatest-embedmongo_2.10"    % "0.2.2"                     % "test"
      val scalaMock        = "org.scalamock"           %% "scalamock-scalatest-support" % "3.2.2"                     % "test"
      val mockito          = "org.mockito"             %  "mockito-core"                % "1.9.5"                     % "test"
    }

  }
}