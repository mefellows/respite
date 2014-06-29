import reactivemongo.api.MongoDriver

/**
 * Created by mfellows on 6/04/2014.
 */
//def connect() {

  println("Connecting to driver.")

  // gets an instance of the driver
  // (creates an actor system)
  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))
//  //
//  //    // Gets a reference to the database "plugin"
  val db = connection("plugin")
//  //
//  //    // Gets a reference to the collection "acoll"
//  //    // By default, you get a BSONCollection.
  val collection = db("acoll")
//}