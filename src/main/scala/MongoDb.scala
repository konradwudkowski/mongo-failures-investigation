import java.util.UUID

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands._
import reactivemongo.api.commands.GetLastError.Majority
import reactivemongo.api.{FailoverStrategy, MongoConnectionOptions, MongoDriver}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.sys.process._
import scala.util.Try


class MongoDb(dbName: String = "test",
              collectionName: String = "testCollection",
              dropCollectionBeforeStarting: Boolean = true) {

  val failoverStrategy =
    FailoverStrategy(
      initialDelay = 500.milliseconds,
      retries = 15,
      delayFactor = attemptNumber => 1 + attemptNumber * 0.5
    )

  val driver = new MongoDriver

  val writeConcern = System.getProperty("write-concern") match {
    case "majority" => GetLastError(Majority, false, false, None)
    case _ => WriteConcern.Default
  }

  val connection = driver.connection((1 to 5).toList.map("localhost:3000" + _),
    MongoConnectionOptions(writeConcern = writeConcern))
  val db = Await.result(connection.database(dbName, failoverStrategy), 20.seconds)

  val collection = db.collection[BSONCollection](collectionName)
  if (dropCollectionBeforeStarting) {
    Await.result(collection.drop(failIfNotFound = false), 5.seconds)
  }

  val primaryNodeContainerName = "mongo1"

  var primaryNodeNotKilledYet = true

  def killPrimary() = {
    if (primaryNodeNotKilledYet) {
      println("\nkilling primary node")
      s"docker kill $primaryNodeContainerName".!!
      primaryNodeNotKilledYet = false
    }
  }

  def insertDocument(): Try[String] = Try {
    val docId = UUID.randomUUID().toString
    val doc = BSONDocument("_id" -> docId)
    val res = Await.result(collection.insert(doc), 5.seconds)
    if (res.ok && res.n == 1) {
      docId
    } else {
      sys.error("insertion failed, writeResult was: " + res)
    }
  }

  def getAllDocuments = Await.result(
    collection.find(BSONDocument.empty).cursor[BSONDocument]().collect[Array](), 20.seconds
  ).map(_.getAs[String]("_id").get)

}
