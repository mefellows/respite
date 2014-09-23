---
layout: default
title: Deployment
---

# Deployment

<p class="message">
  Respite follows the <a href="http://12factor.net/">12 Factor</a> principles. If you're not familiar with these concepts, it's worth taking a short read-through first.
</p>

Respite packages artifacts into an executable Jar, with the JRE as its only runtime dependency. To create one, from within your project dir run `sbt assembly` to package the application.

Then, from your environment, Heroku, etc., ensure the appropriate [environment](http://12factor.net/config) variables are set and run the application with `java -jar <path/to/output-file>.jar`

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

<p class="message">Heroku is a great PaaS that makes deploying and managing applications a breeze. But best of all - you can deploy Respite Apps on Heroku <strong>for free</strong>! The following section assumes you have an Account on Heroku and have the <a href="https://toolbelt.heroku.com/">toolbelt</a> installed.

</a>

Respite works on Heroku out-of-the-box and contains a pre-configured [Procfile](https://devcenter.heroku.com/articles/procfile).

The following creates a new Heroku App, configures a MongoDB instance and deploys a Respite project:

```
heroku apps:create my-respite-application
heroku addons:remove heroku-postgresql:my-respite-application
heroku addons:add mongohq
heroku config:set "DATABASE_URL=$(heroku config:get MONGOHQ_URL)"
git push heroku master
```

You should see output like the following:

```
$ git push heroku master
Initializing repository, done.
Counting objects: 60, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (44/44), done.
Writing objects: 100% (60/60), 10.86 KiB | 0 bytes/s, done.
Total 60 (delta 6), reused 0 (delta 0)

-----> Scala app detected
-----> Installing OpenJDK 1.7...done
-----> Downloading SBT...done
-----> Running: sbt compile stage
       Getting org.scala-sbt sbt 0.13.5 ...
       downloading http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt/0.13.5/jars/sbt.jar ...
        [SUCCESSFUL ] org.scala-sbt#sbt;0.13.5!sbt.jar (61ms)
       downloading http://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.4/scala-library-2.10.4.jar ...

       ...
       ... Downloading the Internet, please wait...
       ...

       [info] Wrote start script for mainClass := Some(au.com.respite.JettyLauncher) to /tmp/scala_buildpack_build_dir/target/start
       [success] Total time: 1 s, completed Sep 23, 2014 4:09:26 AM
-----> Discovering process types
       Procfile declares types -> web

-----> Compressing... done, 256.7MB
-----> Launching... done, v6
       http://my-respite-application.herokuapp.com/ deployed to Heroku

To git@heroku.com:my-respite-application.git
 * [new branch]      master -> master
```

You should now be able to access your application at `http://my-respite-application.herokuapp.com`!