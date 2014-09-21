---
layout: default
title: Monitoring & Metrics
---

# Monitoring & Metrics

All good micro-services require a standard way of measuring & monitoring system performance, providing health checks and so on - Respite comes with first-class support for this with [@coda](https://twitter.com/coda)`s [Metrics](https://dropwizard.github.io/metrics/3.1.0/) library.

## Health Checks

Health checks are just that - they confirm if a service is available, healthy and ready to service clients or not.You can find them at `/metrics/health`

```
$ curl -X GET -H"content-type: application/json" "http://localhost:8080/metrics/health"
{"com.example.app.controller.MyModelController.list":{"healthy":true}}
```

### Creating custom Health Checks

By default, `RestController` objects contain a default health check that queries `"/"`(all) which is executed when the metrics, however if you would like to create a custom monitor simply wrap the check in a `healthCheck` call as per below:


```scala
  /**
   * Create default health check on REST controller - confirm CRUD pipelines are active
   * (what exactly does 'active' mean?)
   */
  healthCheck("doSomething", s"doSomething method in $metricBaseName") {
    def check(): Boolean = {
      // Default to failed health check
      false

      try {
        // Perform some operation
        // ...

        // True will mark health check up
        true
      } catch {
        // False will fail the health check
        false
      }
    }

    // See https://github.com/erikvanoosten/metrics-scala/blob/master/docs/HealthCheckManual.md#warning-for-version-31x-and-earlier
    check()
  }
```

Note: the check() function here is necessary due to an issue with the underlying framework. The variable `$metricBaseName` is available and refers to the parents class name, and is useful for identifying the originating class `MetricsSupport` was mixed in to.

<hr/>

## Runtime Metrics

Respite by default will instrument & measure calls to all `Routes` on a given `Controller` using things called `Timers`. For CRUD services (aka `RestController`s), it will give sensible names to each timer such as 'list', 'delete' and 'get'. For non-`RestController`s, it will use the path provided in the URL as the name of the `Timer`:

Runtime metrics for instrumented Controllers are available at `/metrics/`:

```
$ curl -X GET -H"content-type: application/json" "http://localhost:8080/metrics/"
{
    "version": "3.0.0",
    "gauges": {},
    "counters": {},
    "histograms": {},
    "meters": {},
    "timers": {
        "com.example.app.controller.MyModelController.list": {
            "count": 10000,
            "max": 0.008023,
            "mean": 0.00009569163424124514,
            "min": 0.000043,
            "p50": 0.00006,
            "p75": 0.000063,
            "p95": 0.000087,
            "p98": 0.00012203999999999952,
            "p99": 0.00029594000000000056,
            "p999": 0.00802068,
            "stddev": 0.0004458142701117825,
            "m15_rate": 1630.4,
            "m1_rate": 1630.4,
            "m5_rate": 1630.4,
            "mean_rate": 1596.013541217289,
            "duration_units": "seconds",
            "rate_units": "calls/second"
        }
    }
}
```

<p class="message">
  For further reading on Metrics library, visit the <a href="http://metrics.dropwizard.io/">documentation</a> on the DropWizard website.
</p>