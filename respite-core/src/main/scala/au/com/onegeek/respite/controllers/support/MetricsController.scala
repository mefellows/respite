/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Matt Fellows (OneGeek)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package au.com.onegeek.respite.controllers.support

import javax.servlet.{ServletRegistration, ServletContext, ServletConfig}

import au.com.onegeek.respite.controllers.RespiteApiStack
import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.servlets._
import org.scalatra.{ScalatraServlet, Handler}
import scala.collection.JavaConverters._
import org.eclipse.jetty.server.handler.HandlerCollection

/**
 * REST API and UI for system instrumentation.
 *
 * Provides Health checks, ping, thread dumps and the full gamit of CodeHale's Metrics.
 *
 * @param path The base path to prefix the range of API endpoints from.
 */
class MetricsController(path: String) extends ScalatraServlet with LoggingSupport with MetricsSupport {

  /**
   * Dynamically add all of the CH Servlets into the runtime
   *
   * @param config
   */
  override def init(config: ServletConfig) {
    logger.debug("Initialising MetricsController for mo-metrics!")
    val context = config.getServletContext

    context.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry)
    context.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, registry)

    val metricsServlet = new MetricsServlet(metricRegistry)
    val healthServlet = new HealthCheckServlet(registry)
    val pingServlet = new PingServlet
    val threadDumpServlet = new ThreadDumpServlet

    val adminServlet: AdminServlet = new AdminServlet
    val admin: ServletRegistration.Dynamic = context.addServlet("metricsadmin", adminServlet)
    val metrics: ServletRegistration.Dynamic = context.addServlet("metrics", metricsServlet)
    val health: ServletRegistration.Dynamic = context.addServlet("health", healthServlet)
    val ping: ServletRegistration.Dynamic = context.addServlet("ping", pingServlet)
    val threads: ServletRegistration.Dynamic = context.addServlet("threads", threadDumpServlet)

    // Set mapping
    metrics.addMapping(s"$path/");
    health.addMapping(s"$path/health");
    ping.addMapping(s"$path/ping");
    threads.addMapping(s"$path/threads");
    admin.addMapping(s"$path/admin");

    // Init
    adminServlet.init(config)
    metricsServlet.init(config)
    healthServlet.init(config)
    pingServlet.init(config)
    threadDumpServlet.init(config)

    super.init(config)
    initialize(config)
  }


  // We do this so Scalatra tells us what we're missing out on if the wrong URL is hit...

  get("/") {}

  get("/health") {}

  get("/ping") {}

  get("/threads") {}

}




// Cam was really interesting, basically described an ideal job role
//    Tech generalist
//    70/30 Technical vs  Other,
//    occasional legacy work but still want to make a difference and improve the joint
//    Backed up directors = good guys


// What is future growth opportunities in the team? It's not about the money (I'm not getting paid more, but want to understand what we are looking at)
// Ultimately, CTO is where i'm headed, I can see this role helping me along that path.
// Assessing a couple of other opps - can I have a few moredays? (contracts / consulting etc.)
//


