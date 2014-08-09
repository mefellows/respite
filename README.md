# Respite

REST should be easy - Respite is a reactive & modular micro-framework for REST applications written in Scala.

[![Build Status](https://travis-ci.org/mefellows/respite.svg)](https://travis-ci.org/mefellows/respite)
[![Coverage Status](https://coveralls.io/repos/mefellows/respite/badge.png?branch=feature/caching)](https://coveralls.io/r/mefellows/respite?branch=feature/caching)

## Features

* API usage metrics
* Extensible REST caching support (via CachingSupport with EHCache and in-memory Spray caching)
* Database integration, with CRUD out-of-the-box, via Reactive Mongo and Akka
* In-built JSON <-> Case Class marshalling & validation via play-json

Todo

* OAuth 2.0 Security integration

## Getting Started

Fetch from Maven Central, currently only Snapshots are available:

In your build.{sbt, scala}:

```scala
libraryDependencies += "au.com.onegeek" %% "respite" % "0.0.1"
```
### Create a Model
```scala
case class User(id: BSONObjectID = BSONObjectID.generate, username: String, firstName: String) extends Model[BSONObjectID]

object User {
  import au.com.onegeek.respite.models.ModelJsonExtensions._
  implicit val format = modelFormat { Json.format[User] }
}
```

### Create a Repository

Here is a Repository definition that saves data to a Mongo Database (using the [Reactive Mongo](http://reactivemongo.org/) library), creating an Index on the ```username``` field.

```scala
class UserRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[User, BSONObjectID]("users", mc.db, modelFormatForMongo {Json.format[User]}, ReactiveMongoFormats.objectIdFormats) {

  override def ensureIndexes() = {
    collection.indexesManager.ensure(Index(Seq("username" -> IndexType.Ascending), name = Some("keyFieldUniqueIdx"), unique = true, sparse = true))
  }
}
```

### Add RestController instance to your Scalatra Bootstrap file

Create an instance of RestController for a ```User``` in table "users" on path "/users/*":

```scala
class ScalatraBootstrap extends LifeCycle {
  protected implicit def executor: ExecutionContext = ExecutionContext.global

  override def init(context: ServletContext) {
  
    // Import implicit definitions into Scope
    implicit val bindingModule = ProductionConfigurationModule  // DI Configuration object
    import au.com.onegeek.respite.models.ModelJsonExtensions._  // JSON extensions
    
    // Add Controllers
    addServlet(new RestController[User, BSONObjectID]("users", User.format, new UserRepository), "/users/*")
  }
}
```

### Your done! cURL until your heart is content

    curl "http://localhost:8080/users/"

    [
        {
            "id": {
                "$oid": "53aed383b65f2a89219ddcfd"
            },
            "username": "bmurray",
            "firstName": "Bill"
        },
        {
            "id": {
                "$oid": "53af811bb65f2a89219ddd08"
            },
            "username": "Matty",
            "firstName": "Poopoohead"
        }
    ]

### Add Support for ...

Use the following Mixins to enhance your Controllers

* CachingSupport - to provide automatic Route caching & an ad-hoc Caching DSL within your routes
* MetricSupport - for detailed instrumentation of API calls with its own API + Administration user interface

## Roadmap

* Metrics
 * GUI with pretty charts and stuff
 * Persistent metrics sink
* Oauth 2.0 Integration
 * Provider Server
 * Client Server
* HateosSupport - to link data models together (not required for Mongo setup)
* PaginationSupport - to enable REST pagination on CRUD objects


## Status

This is early stages and subject to dramatic change - use at your own risk.


## API Documentation

http://mefellows.github.io/respite/latest/api/

## Built on top of giants...

We use the following components so you know it's made of good:

* [Scalatra] (http://www.scalatra.org)
* [Reactive Mongo] (http://reactivemongo.org/)
* [Play JSON library] (http://www.playframework.com/documentation/2.1.1/ScalaJson)
* [Metrics](https://github.com/codahale/metrics)

## Livin' on the edge?

To get nightly/development versions, add the following snapshot repository and version:

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "au.com.onegeek" %% "respite" % "0.0.1-SNAPSHOT"
```

## Contributing

Simply fork the repo, create a feature branch and then submit a pull request (oh, please squash your commits too!).