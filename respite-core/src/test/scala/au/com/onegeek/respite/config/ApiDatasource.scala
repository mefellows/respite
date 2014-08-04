package au.com.onegeek.respite.config

import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import org.slf4j.LoggerFactory

/**
 * Created by mfellows on 6/04/2014.
 */
//trait ApiDatasource {
object ApiDatasource {

  val logger = LoggerFactory.getLogger(getClass)

  //  private lazy val database: DefaultDB = connect()

  def connect(): DefaultDB = {

    //    if (database != null) {
    // gets an instance of the driver
    //

    // connect to the replica set composed of `host1:27018`, `host2:27019` and `host3:27020`
    // and authenticate on the database `somedb` with user `user123` and password `passwd123`
    //    var uri: String = "mongodb://@localhost:27017/metrics"
    var uri: String = "mongodb://heroku:ubTmMfDesWyC798mpQnnhRU2VBtgK_Oe-poa4EQMcULqaNmd0ZuAlLfJJr4pOiCfRCEmKV8IGjahw06luSde5w@oceanic.mongohq.com:10084/app23816013"

    if (System.getenv("MONGOHQ_URL") != null) {
      uri = System.getenv("MONGOHQ_URL")
    }
    logger.info("Connecting to MongoDB with URI: " + uri)

    // (creates an actor system)
    val driver = new MongoDriver
    val conn: Try[MongoConnection] =
      MongoConnection.parseURI(uri).map {
        parsedUri =>
          driver.connection(parsedUri)
      }
    conn match {
      case Success(connection) =>
        logger.debug("connected to DB: " + uri)
        connection("metrics")
      case Failure(e) =>
        logger.error("Unable to connect to db with URI: " + uri + ". Error message: " + e.getMessage)
        throw new Exception("Unable to connect to db. Error message: " + e.getMessage)
    }
  }

  def getConnection: DefaultDB = {
    var database = "metrics"
    var uri: String = "mongodb://localhost:17017/"

    if (System.getenv("MONGOHQ_URL") != null) {
      uri = System.getenv("MONGOHQ_URL")
    }
    if (System.getenv("MONGO_DATABASE") != null) {
      database = System.getenv("MONGO_DATABASE")
    }
    uri = uri + database
    logger.info("Connecting to MongoDB with URI: " + uri)

    // (creates an actor system)
    val driver = new MongoDriver
    val conn: Try[MongoConnection] =
      MongoConnection.parseURI(uri).map {
        parsedUri =>
          driver.connection(parsedUri)
      }
    conn match {
      case Success(connection) =>
        logger.debug("connected to DB: " + uri)
        connection(database)
      case Failure(e) =>
        logger.error("Unable to connect to db with URI: " + uri + ". Error message: " + e.getMessage)
        throw new Exception("Unable to connect to db. Error message: " + e.getMessage)
    }
  }

//  def getBlockingConnection: MongoDB = {
//    var database = "metrics"
//    var uri: String = "mongodb://localhost:17017/"
//
//    if (System.getenv("MONGOHQ_URL") != null) {
//      uri = System.getenv("MONGOHQ_URL")
//    }
//    if (System.getenv("MONGO_DATABASE") != null) {
//      database = System.getenv("MONGO_DATABASE")
//    }
//    uri = uri + database
//    logger.info("Connecting to MongoDB with URI: " + uri)
//
//    val mongoClient = MongoClient(MongoClientURI(uri))
//    mongoClient(database)
//  }
}