# Respite

REST should be easy - Respite is a reactive & modular micro-framework for REST applications written in Scala.

## Features

* Easy OAuth2.0 integration
* API usage metrics (With an API and GUI)
* Extensible REST/CRUD Pipeline caching support (via CachingSupport with EHCache and in-memory Spray caching)
* Distributed Database Access and Integration Layer via Reactive Mongo and Akka, with CRUD out-of-the-box
* In-built JSON validation via Spray-json

## Getting Started

Fetch from Maven Central (to be confirmed), currently only Snapshots are available:

In your build.{sbt, scala}:

    libraryDependencies ++= Seq(
      "au.com.onegeek" %% "respite" % "0.0.1-SNAPSHOT",
      "com.typesafe.play" %% "play-json" % "2.2.3"
    )

### Create a Model

    case class User(_id: Option[BSONObjectID] = Some(BSONObjectID.generate), username: String, firstName: String) extends Model
    implicit val UserFormat = Macros.handler[User]

### Add RestController instance to your Scalatra Bootstrap

Create an instance of RestController for a ```User``` in table "users" on path "/users/*":

    context.mount(new RestController[User]("users"), "/users/*")

### Your done

    curl -v "http://localhost:8080/users/"

    [
        {
            "_id": {
                "$oid": "53aed383b65f2a89219ddcfd"
            },
            "username": "bmurray",
            "firstName": "Bill"
        },
        {
            "_id": {
                "$oid": "53af811bb65f2a89219ddd08"
            },
            "username": "Matty",
            "firstName": "Poopoohead"
        }
    ]

### Add Support for ...

Use the following Mixins to enhance your

* CachingSupport - to provide access to a Caching DSL within your routes
* HateosSupport - to link data models together (not required for Mongo setup)
* PaginationSupport - to enable REST pagination on CRUD objects
* RepositorySupport - for enhanced Repository access


## Status

This is very early stages and subject to dramatic change - use at your own risk.

## Built on top of giants...

Thanks to the following projects for making this so awesome:

* Scalatra
* Reactive Mongo
* Play JSON library
* Metrics