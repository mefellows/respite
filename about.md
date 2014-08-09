---
layout: page
title: About
---

<p class="message">
  Hey there! This page is included as an example. Feel free to customize it for your own use upon downloading. Carry on!
</p>

In the novel, *The Strange Case of Dr. Jeykll and Mr. Hyde*, Mr. Poole is Dr. Jekyll's virtuous and loyal butler. Similarly, Poole is an upstanding and effective butler that helps you build Jekyll themes. It's made by [@mdo](https://twitter.com/mdo).

There are currently two themes built on Poole:

* [Hyde](http://hyde.getpoole.com)
* [Lanyon](http://lanyon.getpoole.com)

Learn more and contribute on [GitHub](https://github.com/poole).

{% highlight scala %}
/**
 *
 */
trait MetricsSupport extends ScalatraBase with Metrics with LoggingSupport {
  def getTimer(path: String, method: HttpMethod): Timer = {
    path match {
      case "/" if method == Get => metrics.timer("list")
      case "/:id" if method == Get => metrics.timer("single")
      case "/" if method == Post => metrics.timer("create")
      case "/:id" if method == Post => metrics.timer("update")
      case "/:id" if method == Put => metrics.timer("update")
      case "/:id" if method == Delete => metrics.timer("delete")
      // Ideally capitalise/camelCase this. Also avoid collisions from above.
      case _ => metrics.timer(method.toString.toLowerCase, path.toString.drop(1).replaceAll("[\\/]", "_"))
    }
  }

  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val path = transformers.foldLeft("")((path, transformer) => path.concat(transformer.toString()))

    logger.debug(s"Instrumenting path $path on ${metricBaseName.name}")

    super.addRoute(method, transformers, {
      getTimer(path, method).time {
        logger.debug(s"Instrumenting Action")
        action
      }
    })
  }
}
{% endhighlight %}

## Setup

Some fun facts about the setup of this project include this:

* Built for [Jekyll](http://jekyllrb.com)
* Developed on GitHub and hosted for free on [GitHub Pages](https://pages.github.com)
* Coded with [Sublime Text 2](http://sublimetext.com), an amazing code editor
* Designed and developed while listening to music like [Blood Bros Trilogy](https://soundcloud.com/maddecent/sets/blood-bros-series)

Have questions or suggestions? Feel free to [open an issue on GitHub](https://github.com/poole/issues/new) or [ask me on Twitter](https://twitter.com/mdo).

Thanks for reading!
