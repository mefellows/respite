import respite.Dependencies
import com.typesafe.sbt.SbtGit._

name := "respite-examples"

libraryDependencies ++= Seq(
  Dependencies.Compile.scalatra,
  Dependencies.Compile.scalate,
  Dependencies.Compile.logback,
  Dependencies.Compile.Web.jettyServlet,
  Dependencies.Compile.Web.jettyWebapp,
  Dependencies.Compile.Web.jettyPlus,
  Dependencies.Compile.Test.scalatraTest
)

//libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"

versionWithGit

// Optionally:
git.baseVersion := "0.1"