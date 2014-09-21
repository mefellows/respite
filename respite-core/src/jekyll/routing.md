---
layout: default
title: Routing
---

# Routing

<p class="message">
  Respite is built on top of Scalatra, a Scala port of Ruby's Sinatra. For background and further detail on the routing DSL refer to the <a href="http://scalatra.org/2.3/guides/http/routes.html">docs</a>.
</p>

Respite controllers come standard with JSON<->Case Class marshalling, including field level validation.

## REST Controllers

For out-of-the-box CRUD services, create and register an instance of a `RestController'.

The following RestController for a ```User``` in table "users" on path "/users/*":

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