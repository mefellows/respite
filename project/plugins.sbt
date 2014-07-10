//addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

//addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.2")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.10.0")

//addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.8.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")