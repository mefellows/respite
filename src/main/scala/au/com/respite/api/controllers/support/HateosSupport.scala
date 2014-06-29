package au.com.respite.api.controllers.support

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
  case class Link(href: String, method: String)

  type HateoasLinks = Map[String, Link]

  //case class for a response containing a Collection of items
  case class ListResponse(_links: HateoasLinks, _embedded: Map[String, List[Any]])

  object HateoasLinkFactory {
    //could (should) add a function for generating a "custom" action link
    def createSelfLink(uri: String) = {
      ("self" -> new Link(uri, "GET"))
    }

    //create Create!
    def createCreateLink(uri: String) = {
      ("create" -> new Link(uri, "POST"))
    }

    def createUpdateLink(uri: String) = {
      ("update" -> new Link(uri, "PUT"))
    }

    def createDeleteLink(uri: String) = {
      ("delete" -> new Link(uri, "DELETE"))
    }
  }

}