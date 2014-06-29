# Respite

REST should be easy - Respite is a reactive & modular micro-framework for REST applications written in Scala.

# Getting Started

(to be confirmed)

Fetch from Maven Central, currently only Snapshots are available:

In your build.{sbt, scala}:

    libraryDependencies ++= Seq(
      "au.com.onegeek" %% "respite" % "0.0.1-SNAPSHOT",
      "com.typesafe.play" %% "play-json" % "2.2.3" //supports from 2.1.0
    )


## Status

This is very early stages and subject to dramatic change - use at your own risk.

# Built on top of giants...

Thanks to the following projects for making this so awesome:

* Scalatra
* Reactive Mongo
* Play JSON library