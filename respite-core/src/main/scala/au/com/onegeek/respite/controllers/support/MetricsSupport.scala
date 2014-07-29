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

import nl.grons.metrics.scala.{MetricName, InstrumentedBuilder, CheckedBuilder}

/**
* Metrics and HealthCheck Support.
*
* Mixin this Trait into any class, Controller etc. to gain access to A metrics
* and health-check DSL, provided by https://github.com/erikvanoosten/metrics-scala/.
*
* {{{RestControllers}}} have this out-of-the-box so is only required on non-Respite
* family objects.
*
* Created by mfellows on 23/06/2014.
*/
trait MetricsSupport  extends InstrumentedBuilder with CheckedBuilder {
  override lazy val metricBaseName = MetricName(getClass)
  val metricRegistry = RespiteApplicationMetrics.metricRegistry
  val registry = RespiteApplicationMetrics.healthChecksRegistry
}

object RespiteApplicationMetrics {
  val healthChecksRegistry = new com.codahale.metrics.health.HealthCheckRegistry();
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
}