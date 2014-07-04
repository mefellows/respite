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
import _root_.akka.actor.ActorSystem
import au.com.onegeek.respite.config.ProductionConfigurationModule
import org.scalatra._
import javax.servlet.ServletContext
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext

/**
 * Main Scalatra entry point.
 */
class ScalatraBootstrap extends LifeCycle {
  protected implicit def executor: ExecutionContext = ExecutionContext.global

  val logger = LoggerFactory.getLogger(getClass)

  // Add implicit Binding Module in here....

  // Get a handle to an ActorSystem and a reference to one of your actors
  val system = ActorSystem()
  override def init(context: ServletContext) {
    implicit val bindingModule = ProductionConfigurationModule

  }

  // Make sure you shut down
  override def destroy(context:ServletContext) {
    system.shutdown()
  }
}