# Respite

REST should be easy - Respite is a reactive & modular micro-framework for REST applications written in Scala.

[![Build Status](https://travis-ci.org/mefellows/respite.svg)](https://travis-ci.org/mefellows/respite)
[![Coverage Status](https://coveralls.io/repos/mefellows/respite/badge.png?branch=multi-module)](https://coveralls.io/r/mefellows/respite?branch=multi-module)

## Features

* Easy OAuth2.0 integration
* API usage metrics (With an API and GUI)
* Extensible REST/CRUD Pipeline caching support (via CachingSupport with EHCache and in-memory Spray caching)
* Distributed Database Access and Integration Layer via Reactive Mongo and Akka, with CRUD out-of-the-box
* In-built JSON <-> Case Class marshalling & validation via play-json

## Getting Started

Fetch from Maven Central (to be confirmed), currently only Snapshots are available:

In your build.{sbt, scala}:

```scala
resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Akka Repo" at "http://repo.akka.io/repository"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "au.com.onegeek" %% "respite" % "0.1.0-SNAPSHOT"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.2.3"
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
/**
 * Main Scalatra entry point.
 */
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

### Your done

    curl -v "http://localhost:8080/users/"

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

* CachingSupport - to provide access to a Caching DSL within your routes
* HateosSupport - to link data models together (not required for Mongo setup)
* PaginationSupport - to enable REST pagination on CRUD objects
* RepositorySupport - for enhanced Repository access
* MetricSupport - for detailed instrumentation of API calls with its own API + Administration user interface

## Status

This is very early stages and subject to dramatic change - use at your own risk.

## Built on top of giants...

Thanks to the following projects for making this so awesome:

* Scalatra
* Reactive Mongo
* Play JSON library
* Metrics


## Contributing

Simply fork the repo, create a feature branch and then submit a pull request.
