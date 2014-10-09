# Respite

REST should be easy - Respite is a reactive & modular micro-framework for REST applications written in Scala.

[![Build Status](https://travis-ci.org/mefellows/respite.svg?branch=feature%2Fcaching-headers)](https://travis-ci.org/mefellows/respite)
[![Coverage Status](https://coveralls.io/repos/mefellows/respite/badge.png?branch=feature%2Fcaching-headers)](https://coveralls.io/r/mefellows/respite?branch=feature%2Fcaching-headers)

## Features

* Designed for [microservices](http://martinfowler.com/articles/microservices.html) architectures following the [12 factor](http://12factor.net/) principles
* [Sinatra](http://sinatrarb.com/)-esque Routing DSL courtesy of [Scalatra](http://scalatra.org)
* Extensible REST caching support & DSL
* API usage metrics
* Database integration, with CRUD out-of-the-box, via Reactive Mongo and Akka
* API Key Security
* In-built JSON <-> Case Class marshalling & validation via play-json
* ...and more!

## Getting Started

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

### Scaffolding

From within sbt run `g8Scaffold <TAB>` to see what can be auto-generated for you:

**Creating your first CRUD Service**

This command will generate for you a `Model` and accompanying `Repository` & `Controller` classes along with stubbed test cases. It is the best way to get started learning Respite. Feel free to update your `Model` case class when you're done:

```
> g8Scaffold crud-service
model_name [MyModel]: Car
organisation [com.example.app]: com.example.app.carmaker
Success :)
```

To make this service active, you will need to [register](http://respite.onegeek.com.au/routing/#register) your `Controller` in `ScalatraBootstrap` as follows:

```scala
class ScalatraBootstrap extends LifeCycle {
  protected implicit def executor: ExecutionContext = ExecutionContext.global

  override def init(context: ServletContext) {

    // Import implicit definitions into Scope
    implicit val bindingModule = ProductionConfigurationModule  // DI Configuration object
    import au.com.onegeek.respite.models.ModelJsonExtensions._  // JSON extensions

    // Add your Controller here...
    context.mount(new UserController(new UserRepository), "/users/*")
  }
}
```

To hot-reload the changes into the running app, run `~ ;copy-resources;aux-compile` in SBT.

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

### What next?

Follow the [guide](http://respite.onegeek.com.au/) for a more comprehensive tutorial, or take a look at a working [example](https://github.com/mefellows/respite/tree/master/respite-examples).

### Mixin some magic to spice things up...

Use the following Mixins to enhance your Routes:

* Authentication - for standard API Key security for your Routes
* CachingRouteSupport - to provide automatic CRUD & idempotent route caching & a simple caching DSL
* CachingSupport - for a simple caching DSL on non-RESTy routes
* MetricSupport - for automatic & detailed instrumentation and health checks API for your API calls and routes

## Roadmap

* Implementation of common fault tolerance and resilience features (such as the likes of Hystrix)
* Metrics
 * GUI with pretty charts and stuff
 * Persistent metrics sink
* Oauth 2.0 Integration
 * Provider Server
 * Client Server
* HateosSupport - to link data models together (not required for Mongo setup)
* PaginationSupport - to enable REST pagination on CRUD objects

## Status

This is early stages but the core API is now much more stable. Continue to use with caution.

## Documentation

* [Project Documentation](http://respite.onegeek.com.au/)
* [Example Project](https://github.com/mefellows/respite/tree/master/respite-examples)
* [API documentation](http://respite.onegeek.com.au/latest/api/#package)
* [Test Specifications](http://respite.onegeek.com.au/latest/specifications/)

## Built on top of giants...

We use the following components so you know it's made of good:

* [Scalatra](http://www.scalatra.org)
* [Reactive Mongo](http://reactivemongo.org/)
* [Play JSON library](http://www.playframework.com/documentation/2.1.1/ScalaJson)
* [Metrics](https://github.com/codahale/metrics)

## Livin' on the edge?

To get nightly/development versions, add the following snapshot repository and version

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "au.com.onegeek" %% "respite" % "0.3.0-SNAPSHOT"
```

## Contributing

Simply fork the repo, create a feature branch and then submit a pull request (oh, please squash your commits too!).
