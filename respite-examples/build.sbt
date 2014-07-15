import respite.Dependencies

name := "respite-examples"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  Dependencies.Compile.scalatra,
  Dependencies.Compile.scalate,
  Dependencies.Compile.specs2,
  Dependencies.Compile.logback,
  Dependencies.Compile.Web.jettyServlet,
  Dependencies.Compile.Web.jettyWebapp,
  Dependencies.Compile.Test.scalatest,
  Dependencies.Compile.Test.scalatraTest
)

