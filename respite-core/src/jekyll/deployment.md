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

`DATABASE_PORT=17017 DATABASE_HOST=127.0.0.1 DATABASE_NAME=test_foo PORT=8080 java -jar target/scala-2.10/my-project.jar`

## Deployment to Heroku

TODO