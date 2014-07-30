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

/**
 * Created by mfellows on 30/07/2014.
 */
class MetricsController(context: ServletContext) extends ScalatraServlet with MetricsSupport {
//class MetricsController extends RespiteApiStack {


  override def init(config: ServletConfig) {


    // Dynamic add servlet for servlet 3.x or later
    if (context.getMajorVersion() >= 3) {
      println("YO, adding health checks!")
//      val admin: ServletRegistration.Dynamic = context.addServlet("metricsadmin", new AdminServlet)
      val metrics: ServletRegistration.Dynamic = context.addServlet("metrics", new MetricsServlet(metricRegistry))
      val health: ServletRegistration.Dynamic = context.addServlet("health", new HealthCheckServlet(registry))
      val ping: ServletRegistration.Dynamic = context.addServlet("ping", new PingServlet)
      val threads: ServletRegistration.Dynamic = context.addServlet("threads", new ThreadDumpServlet)
      metrics.addMapping("/metrics/");
      health.addMapping("/metrics/health");
      ping.addMapping("/metrics/ping");
      threads.addMapping("/metrics/threads");
//      admin.addMapping("/metrics/admin");
    }

    super.init(config)
    initialize(config) // see Initializable.initialize for why
  }


  get("/foo") {
    println("foo'd me")

    val results: java.util.Map[String, HealthCheck.Result] = registry.runHealthChecks()

    results.asScala.foreach( { s =>
      println(s)
    })

//    for (Entry<String, HealthCheck.Result> entry : results.entrySet()) {
//      if (entry.getValue().isHealthy()) {
//        System.out.println(entry.getKey() + " is healthy");
//      } else {
//        System.err.println(entry.getKey() + " is UNHEALTHY: " + entry.getValue().getMessage());
//        final Throwable e = entry.getValue().getError();
//        if (e != null) {
//          e.printStackTrace();
//        }
//      }
//    }


  }

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


