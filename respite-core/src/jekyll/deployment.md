---
layout: page
title: Deployment
---

<p class="message">
  Respite follows the <a href="http://12factor.net/">12 Factor</a> principles. If you're not familiar with these concepts, it's worth taking a short read-through first.
</p>

Respite packages artifacts into an executable Jar. To create one, from within your project dir run `sbt assembly` to package theh application.

Then, from your environment, Heroku, etc., ensure the appropriate [environment](http://12factor.net/config) variables are set and run the Jar.

## Local Deployment Example

`DATABASE_PORT=27017 DATABASE_HOST=127.0.0.1 DATABASE_NAME=test_foo java -jar target/scala-2.10/my-project.jar`

## Deployment to Heroku

TODO