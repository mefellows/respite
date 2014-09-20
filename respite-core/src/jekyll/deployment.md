---
layout: page
title: Deployment
---

<p class="message">
  Respite follows the <a href="http://12factor.net/">12 Factor</a> principles. If you're not familiar with these concepts, it's worth taking a short read-through first.
</p>

Respite packages artifacts into an executable Jar. To create one, from within your project dir run `sbt assembly` to package theh application.

Then, from your environment, Heroku, etc., ensure the appropriate [environment](http://12factor.net/config) variables are set and run the Jar with `java -jar <output-file>.jar`

**Local Deployment Example**

Assuming there is a local database running on port 17017, the following exports the required database environment variables and runs a local embedded Jetty container on port 8080:

`DATABASE_PORT=17017 DATABASE_HOST=127.0.0.1 DATABASE_NAME=test_foo PORT=8080 java -jar target/scala-2.10/example.jar`

Running this command on the respite-examples sub-project shows the following output:

```
11:37:57.752 [main] INFO  org.eclipse.jetty.server.Server - jetty-8.y.z-SNAPSHOT
11:37:57.878 [main] INFO  o.e.j.w.StandardDescriptorProcessor - NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
11:37:57.916 [main] INFO  o.scalatra.servlet.ScalatraListener - The cycle class name from the config: ScalatraBootstrap
11:37:57.967 [main] INFO  o.scalatra.servlet.ScalatraListener - Initializing life cycle class: ScalatraBootstrap
11:37:58.211 [main] DEBUG a.c.o.r.e.models.SimpleAuthServlet - Instrumenting path /token/:key on au.com.onegeek.respite.examples.models.SimpleAuthServlet
11:37:58.212 [main] DEBUG a.c.o.r.e.models.SimpleAuthServlet - Instrumenting path /tokens/ on au.com.onegeek.respite.examples.models.SimpleAuthServlet
11:37:58.213 [main] DEBUG a.c.o.r.e.models.SimpleAuthServlet - Instrumenting path /token/? on au.com.onegeek.respite.examples.models.SimpleAuthServlet
[DEBUG] [09/20/2014 11:37:58.544] [main] [EventStream(akka://reactivemongo)] logger log1-Logging$DefaultLogger started
[DEBUG] [09/20/2014 11:37:58.545] [main] [EventStream(akka://reactivemongo)] Default Loggers started
[DEBUG] [09/20/2014 11:37:58.927] [main] [EventStream] StandardOutLogger started
[DEBUG] [09/20/2014 11:37:58.937] [main] [EventStream(akka://default)] logger log1-Logging$DefaultLogger started
[DEBUG] [09/20/2014 11:37:58.937] [main] [EventStream(akka://default)] Default Loggers started
[DEBUG] [09/20/2014 11:37:58.937] [main] [EventStream(akka://default)] logger log1-Logging$DefaultLogger started
[DEBUG] [09/20/2014 11:37:58.937] [main] [EventStream(akka://default)] Default Loggers started
11:37:58.940 [main] DEBUG a.c.o.r.e.models.UserController - Caching path / on class au.com.onegeek.respite.examples.models.UserController
11:37:58.942 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path / on au.com.onegeek.respite.examples.models.UserController
11:37:58.942 [main] DEBUG a.c.o.r.e.models.UserController - Caching path /:id on class au.com.onegeek.respite.examples.models.UserController
11:37:58.942 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path /:id on au.com.onegeek.respite.examples.models.UserController
11:37:58.943 [main] DEBUG a.c.o.r.e.models.UserController - Caching path / on class au.com.onegeek.respite.examples.models.UserController
11:37:58.944 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path / on au.com.onegeek.respite.examples.models.UserController
11:37:58.945 [main] DEBUG a.c.o.r.e.models.UserController - Caching path /:id on class au.com.onegeek.respite.examples.models.UserController
11:37:58.945 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path /:id on au.com.onegeek.respite.examples.models.UserController
11:37:58.946 [main] DEBUG a.c.o.r.e.models.UserController - Caching path /:id on class au.com.onegeek.respite.examples.models.UserController
11:37:58.946 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path /:id on au.com.onegeek.respite.examples.models.UserController
11:37:58.947 [main] DEBUG a.c.o.r.e.models.UserController - Caching path /:id on class au.com.onegeek.respite.examples.models.UserController
11:37:58.947 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path /:id on au.com.onegeek.respite.examples.models.UserController
11:37:58.947 [main] DEBUG a.c.o.r.e.models.UserController - Caching path /search/ on class au.com.onegeek.respite.examples.models.UserController
11:37:58.947 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path /search/ on au.com.onegeek.respite.examples.models.UserController
11:37:58.968 [main] DEBUG a.c.o.r.e.models.UserController - Caching path /cache/expire on class au.com.onegeek.respite.examples.models.UserController
11:37:58.968 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path /cache/expire on au.com.onegeek.respite.examples.models.UserController
11:37:58.968 [main] DEBUG a.c.o.r.e.models.UserController - Caching path /cache/expire/:key on class au.com.onegeek.respite.examples.models.UserController
11:37:58.969 [main] DEBUG a.c.o.r.e.models.UserController - Instrumenting path /cache/expire/:key on au.com.onegeek.respite.examples.models.UserController
[DEBUG] [09/20/2014 11:37:58.985] [main] [EventStream] StandardOutLogger started
[DEBUG] [09/20/2014 11:37:58.993] [main] [EventStream(akka://default)] logger log1-Logging$DefaultLogger started
[DEBUG] [09/20/2014 11:37:58.994] [main] [EventStream(akka://default)] Default Loggers started
[DEBUG] [09/20/2014 11:37:58.993] [main] [EventStream(akka://default)] logger log1-Logging$DefaultLogger started
[DEBUG] [09/20/2014 11:37:58.994] [main] [EventStream(akka://default)] Default Loggers started
11:37:59.003 [main] DEBUG a.c.o.r.c.support.MetricsController - Initialising MetricsController for mo-metrics!
11:37:59.188 [main] INFO  o.f.s.servlet.ServletTemplateEngine - Scalate template engine using working directory: /var/folders/kf/4sgp93ys2t3_9t3yyzr07c3r0000gn/T/scalate-4688608319125102195-workdir
11:37:59.209 [main] INFO  o.e.jetty.server.AbstractConnector - Started SelectChannelConnector@0.0.0.0:8001
```

## Deployment to Heroku

TODO