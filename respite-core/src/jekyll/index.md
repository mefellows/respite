---
layout: default
title: Home
---

# Respite - REST for micro-services

<p class="message">
  Version 0.2.0 Just released!
</p>

Respite is a micro-framework designed for creating RESTful services in micro-service architectures and reactive web applications. It is fairly opinionated, however built upon the excellent [Scalatra](http://www.scalatra.org/), it is also easily extended.

## Why use Respite?

Respite is not a fully featured framework like [Play](https://www.playframework.com/), [Lift](http://liftweb.net/), [Spring MVC](http://projects.spring.io/spring-framework/) etc. It is a micro-framework designed particularly well for:

* Use in a [micro-services](http://martinfowler.com/articles/microservices.html) architecture
* Speed: Respite is fully asynchronous from top-to-toe - including (MongoDB) database operations - meaning it has excellent parallel execution capabilities
* As a backing REST API in a Reactive JavaScript Application (Angular, React, etc.)
* Rapid, iterative deployment cycles: `idea->build->deploy->measure->learn->idea...`
* Pre-configured for simple [deployment](http://respite.onegeek.com.au/deployment/) to [Heroku](http://www.heroku.com)

<hr/>

## Getting Started

<p class="message">
  The recommended approach is using the Respite Generator, as it comes integrated with a scaffolding engine to assist with rapid development.
</p>

### Using the Respite Generator

Follow these steps to get a basic Respite project up and running.

Respite comes with a scaffolding tool to make creating services a breeze. Before you start, ensure you have access to a Mongo DB instance running on the default port on 127.0.0.1 or modify the provided [.env](https://github.com/mefellows/sbt-dotenv) file overriding environment variables.

1. Install [sbt](http://www.scala-sbt.org/release/tutorial/Setup.html).
2. Install [giter8](https://github.com/n8han/giter8#installation):
  1. `curl https://raw.githubusercontent.com/n8han/conscript/master/setup.sh | sh`.
  2. `~/bin/cs n8han/giter8`.
3. Run `~/bin/g8 mefellows/respite-sbt.g8` and follow the prompts.
4. Launch `sbt` in this directory. Wait patiently as it downloads the Internet the first time.
5. Execute `container:start` to launch the web application.
6. Execute `browse` to see a default HTML template display in your browser - this is nothing exciting.

Once you're up and running, you can use the giter8 [scaffolding](https://github.com/n8han/giter8#scaffolding-plugin) tool to build our your services.

#### Scaffolding

From within sbt run `g8Scaffold <TAB>` to see what can be auto-generated for you:

**Creating CRUD Services**

This command will generate for you a `Model` and accompanying `Repository` & `Controller` classes along with stubbed test cases. It is the best way to get started learning Respite:

```
> g8Scaffold crud-service
model_name [MyModel]: Car
organisation [com.example.app]: com.example.app.carmaker
Success :)
```

To make this service active, you will need to [register](/routing/#register) your `Controller`. See [below](/#register) for an example.

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

<a id="#register"> </a>
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