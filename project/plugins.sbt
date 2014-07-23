addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.2")

//addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.10.0")

//addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.8.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)


// Just until dotenv is published in sbt-plugin-releases repo (above)
resolvers += Resolver.url(
   "mefellows-sbt-plugins",
   url("http://dl.bintray.com/content/mefellows/sbt-plugins"))(
     Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

resolvers += Classpaths.sbtPluginReleases

resolvers += Classpaths.typesafeReleases

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.5.1")

//addSbtPlugin("com.sksamuel.scoverage" % "sbt-scoverage" % "0.95.1")

addSbtPlugin("com.sksamuel.scoverage" %% "sbt-coveralls" % "0.0.5")

// Auto-imported now locally
addSbtPlugin("au.com.onegeek" % "sbt-dotenv" % "1.0.3")

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")