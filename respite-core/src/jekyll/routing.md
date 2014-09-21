---
layout: default
title: Routing
---

# Routing

<p class="message">
  Respite is built on top of Scalatra, a Scala port of Ruby's Sinatra. For background and further detail on the routing DSL refer to the <a href="http://scalatra.org/2.3/guides/http/routes.html">docs</a>.
</p>

Respite controllers provide a powerful routing DSL & come standard with type-safe, JSON to case class conversion, including field-level validation.

Other out-of-the-box features include:

* CRUD for case class entities
* Route Caching & Simple Cache DSL
* Metrics & Health Check Support
* CORS Support
* API Key Authentication
* Output logging

## REST Controllers

For out-of-the-box CRUD services for your [Models](/models), create and register an instance of a `RestController`. This can be done in one of two ways: declaring a new `Class` extending `RestController` (recommended for all but the simplest of controllers) or creating a new value object of type `RestController`:

The following examples highlight the two forms:

**New Class**

```scala
class ProductController(repository: ReactiveRepository[Product, BSONObjectID])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[Product], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[Product, BSONObjectID]("products", Product.format, repository)
```

**Value Object**

```scala
def route = new RestController[Product, BSONObjectID]("products", Product.format, new ProductRepository) with MetricsRestSupport[Product, BSONObjectID], "/products")
```

The latter form is generally only useful for fairly uninteresting CRUD services.

## JSON Services

If you don't require `Model` persistence, mixin the `PlayJsonSupport[T]` Trait, specifying the type that will be converted to/from JSON.

```scala
val playServlet = new TestServlet with PlayJsonSupport[User]
```

<a id="register"> </a>
## Registering Controllers
Once you have created your `Controller`, simply add an instance of one with the `context.mount()` method in your `ScalatraBootstrap` class:

```scala
class ScalatraBootstrap extends LifeCycle {
  protected implicit def executor: ExecutionContext = ExecutionContext.global

  override def init(context: ServletContext) {

    // Import implicit definitions into Scope
    implicit val bindingModule = ProductionConfigurationModule  // DI Configuration object
    import au.com.onegeek.respite.models.ModelJsonExtensions._  // JSON extensions

    // Add Controllers here
    context.mount(new RestController[User, BSONObjectID]("users", User.format, new UserRepository), "/users/")
    context.mount(new MyModelController(new MyRepository), "/mymodels/")
  }
}
```
<hr/>

## API Keys & Authentication
API Keys are a simple way to control access to your APIs, but


### Manually managed keys

Useful during development, these keys are essentially an in-memory map that are not persisted between application restarts.

Create an instance of a `ConfigAuthenticationStrategy` setting the keys in the map:

```scala
// Authentication API with default keys
object ConfigAuthStrategy extends ConfigAuthenticationStrategy {
  override var keys = Map("admin" -> ApiKey(application = "admin", description = "Test App", key = "testkey")) ++
                      Map("murray" -> ApiKey(application = "bill", description = "Foo App", key = "murray"))
}
```

### Database Persisted Keys

Ensure a local Mongo database is available with the table 'apikeys' available. Respite provides a `ApiKeyRepository` class to persist keys, which you can use when creating the `DatabaseAuthenticationStrategy` object:

```scala
val repository = new ApiKeyRepository
override implicit val authenticationStrategy = new DatabaseAuthenticationStrategy(repository)
  ...
}
```

### Mixin the Authentication magic

Once you've created the `AuthenticationStrategy` object, mixin the `Authentication` Trait to your `Controller` and set the `authenticationStrategy` to your new configuration object:

```
class UserController(repository: ReactiveRepository[User, BSONObjectID])(override implicit val bindingModule: BindingModule, override implicit val tag: ClassTag[User], override implicit val objectIdConverter: String => BSONObjectID) extends RestController[User, BSONObjectID]("users", User.format, repository)[User, BSONObjectID] with Authentication {
  override implicit val authenticationStrategy = ConfigAuthStrategy
}
```

### API Key Management REST API

Respite provides a simple REST API to manage API tokens at run time:

<table>
    <tr>
      <th>Method</th><th>Path</th><th>Body</th>
    </tr>
    <tr>
        <td>GET</td><td><servlet>/tokens/ </td><td></td>
    </tr>
    <tr>
        <td>DELETE</td><td><servlet>/tokens/:key</td><td></td>
    </tr>
    <tr>
        <td>POST</td><td><servlet>/tokens/ </td><td>{"application" : "kickass", "key" : "pants", "description":"We sell awesome pants" }</td>
    </tr>
</table>

<hr/>

## Metrics

<p class="message">
  Visit the section <a href="/monitoring/">Monitoring &amp; Metrics</a> for a detailed description of Respite's capabilities in the space.
</p>

`RestController` classes are already instrumented with the typed `MetricsRestSupport[T]` Trait, containing a default health check implementation. For other `Controller` classes, use the `MetricsSupport` Trait.

<hr/>

## Caching

As with metrics, `RestController` classes require a the `CachingRouteSupport` and non-`RestController` `Route`s should use `CachingSupport` Trait. However, unlike Metrics, caching is disabled by default.

### Creating Custom Cache entries

The caching DSL is fairly straightforward, wrapping a block with `cache(key)` will return a `Future[Any]`. The following example wraps the contents of a `POST` operation (something which is not automatically cached for obvious reasons):

```scala
  post("/foobar") {
    cache("es") {
      Future {
        logger.info("About to find all")
        repository.findAll
      }
    }
  }
```

### Expiring Cache Entries

The cache can be completely expired via the `clear` method on the `cache` property. A REST service also exists at `DELETE /cache/` and `DELETE /cache/:key`.

### Overriding caching TTLs

By default, responses are cached forever, unless the cache is invalidated by way of a `DELETE` or `POST` operation.If you'd like to change this behaviour, simply override the following properties of any class instrumented via `CachingSupport`.

```scala
  override val timeToLive = 300 seconds
  override val timeToIdle = 60 seconds
```

<p class="message">
  NOTE: The Caching implementation uses the `scala.concurrent.duration` package for its unit of time declarations.
</p>

<hr/>

## Logging

Mixin `LoggingSupport` to access a Logback `logger` object with the standard methods. By default, this will be sent to `stdout` (see [logs](http://12factor.net/logs) for best practice on 12 factor apps) but can be easily [configured](http://logback.qos.ch/) to log to another location.

```scala
// Creates a route on "/logme" which will log to console on request
get("/logme") {
  logger.debug("This is a debug log on path `/logme`")
}
```