import respite.Dependencies
import com.typesafe.sbt.SbtGit._
import AssemblyKeys._ // put this at the top of the file

name := "respite-examples"

assemblySettings

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  Dependencies.Compile.scalatra,
  Dependencies.Compile.scalate,
//  Dependencies.Compile.specs2,
  Dependencies.Compile.logback,
  Dependencies.Compile.Web.jettyServlet,
  Dependencies.Compile.Web.jettyWebapp,
  Dependencies.Compile.Test.scalatraTest
)

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case "logback.xml" => MergeStrategy.first
  case x => old(x)
}
}

jarName in assembly := "example.jar"

versionWithGit

// Optionally:
git.baseVersion := "0.1"