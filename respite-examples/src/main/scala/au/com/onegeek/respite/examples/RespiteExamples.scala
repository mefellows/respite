package au.com.onegeek.respite.examples

import org.scalatra._
import scalate.ScalateSupport

class RespiteExamples extends RespiteExamplesStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }
  
}
