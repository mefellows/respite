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

/**
 * Created by mfellows on 23/06/2014.
 */
//package object full o' utility functions for creating some HAL-style HATEOAS links


// To be adapted from: http://jamesadam.me/blog/tag/scalatra/
// Need to also add capability to 'navigate' to other objects (maybe use reflection / package
// introspection to see how to 'navigate'?


// TODO: Turn this into a self-type Trait for mixin support into controllers
// TODO: Create a DSL (In trait `Model` To be able to dynamically generate paths in here.
trait HateosSupport {

  //could add an additional field specifying MIME-type, for example
//  case class Link(href: String, method: String)
//
//  type HateoasLinks = Map[String, Link]
//
//  //case class for a response containing a Collection of items
//  case class ListResponse(_links: HateoasLinks, _embedded: Map[String, List[Any]])
//
//  object HateoasLinkFactory {
//    //could (should) add a function for generating a "custom" action link
//    def createSelfLink(uri: String) = {
//      ("self" -> new Link(uri, "GET"))
//    }
//
//    //create Create!
//    def createCreateLink(uri: String) = {
//      ("create" -> new Link(uri, "POST"))
//    }
//
//    def createUpdateLink(uri: String) = {
//      ("update" -> new Link(uri, "PUT"))
//    }
//
//    def createDeleteLink(uri: String) = {
//      ("delete" -> new Link(uri, "DELETE"))
//    }
//  }

}