---
layout: default
title: Home
---

# Respite - REST for micro-services

<p class="message">
  Version 0.2.0 Just released!
</p>

Respite is a micro-framework designed for creating RESTful services in micro-services architectures and reactive web applications. It is fairly opinionated, however built upon the excellent [Scalatra](http://www.scalatra.org/), it is also easily extended.

## Why use Respite?

Respite is not a fully featured framework like [Play](https://www.playframework.com/), [Lift](http://liftweb.net/), [Spring MVC](http://projects.spring.io/spring-framework/) etc. It is a micro-framework designed particularly well for:

* For use in a [micro-services](http://martinfowler.com/articles/microservices.html) architecture
* As a backing REST API in a Reactive JavaScript Application (Angular, React, etc.)

## Getting Started

<p class="message">
  The recommended approach is using the Respite Generator, as it comes integrated with a scaffolding engine to assist with rapid development.
</p>

### Using the Respite Generator

Follow these steps to get a basic Respite project up and running.

1. [Install sbt](http://www.scala-sbt.org/release/tutorial/Setup.html).
2. [Install giter8](https://github.com/n8han/giter8#installation).
3. In a command console, `cd` to your development environment
4. Run `g8 mefellows/respite-sbt.g8`
5. Fill in the prompted blanks. Default values are in the square brackets. Fill in the variables as you see fit, but only mess with the versions if you know what you're doing.
6. `cd` to the newly created directory and do your `git init`, etc.
7. Launch `sbt` in this directory. Wait patiently as it downloads the internet the first time.
8. Execute `container:start` to launch the web application.
9. Execute `browse` to see a default HTML template display in your browser - this is nothing exciting.

Once you're up and running, you can use the giter8 [scaffolding](https://github.com/n8han/giter8#scaffolding-plugin) tool to build our your services.

#### Scaffolding

From within sbt run `g8Scaffold <TAB>` to see what can be auto-generated for you:

**Creating Models**

`g8Scaffold model`

**Creating Repositories**

`g8Scaffold repository`

**Creating Controllers**

`g8Scaffold controller`

### Existing Scalatra Application

If you already have an existing Scalatra application, you can extend it with Respite features:

In your ```build.{sbt, scala}```:

```scala
libraryDependencies += "au.com.onegeek" %% "respite-core" % "0.2.0"
```

#### Create a Model

```scala
case class User(id: BSONObjectID = BSONObjectID.generate, username: String, firstName: String) extends Model[BSONObjectID]

object User {
  import au.com.onegeek.respite.models.ModelJsonExtensions._
  implicit val format = modelFormat { Json.format[User] }
}
```

#### Create a Repository

Here is a Repository definition that saves data to a Mongo Database (using the [Reactive Mongo](http://reactivemongo.org/) library), creating an Index on the ```username``` field.

```scala
class UserRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[User, BSONObjectID]("users", mc.db, modelFormatForMongo {Json.format[User]}, ReactiveMongoFormats.objectIdFormats) {

  override def ensureIndexes() = {
    collection.indexesManager.ensure(Index(Seq("username" -> IndexType.Ascending), name = Some("keyFieldUniqueIdx"), unique = true, sparse = true))
  }
}
```

#### Add RestController instance to your Scalatra Bootstrap file

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