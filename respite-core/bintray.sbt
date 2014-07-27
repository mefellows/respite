//import bintray.Keys._
//import com.typesafe.sbt.SbtGit._
//
//resolvers += Resolver.url(
//  "bintray-sbt-plugin-releases",
//  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
//    Resolver.ivyStylePatterns)
//
//addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")
//
//publishMavenStyle := false
//
//bintrayPublishSettings
//
//repository in bintray := "respite"
//
//licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
//
//bintrayOrganization in bintray := None
//
//versionWithGit
//
//// Optionally:
//git.baseVersion := "0.1"