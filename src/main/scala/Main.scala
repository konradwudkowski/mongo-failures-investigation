import java.time.LocalTime

import Timer._

import scala.concurrent.duration._

object Main extends App {

  val mongo = new MongoDb()

  var insertsAcknowledgedByMongo = List.empty[String]
  val testDuration = 15.seconds


  implicit val startTime = LocalTime.now()

  println("\nWriting to mongo...")
  while (timeNotExceeded(testDuration)) {
    mongo.insertDocument().foreach { docId =>
      insertsAcknowledgedByMongo = docId :: insertsAcknowledgedByMongo
    }
    if (timeReached(testDuration - 2.seconds)) {
      mongo.killPrimary()
    }
  }

  println("\ncalculating...")
  val currentlyInMongo = mongo.getAllDocuments

  val elementsInsertedButNoLongerFound =
    insertsAcknowledgedByMongo.toArray.filterNot(e => currentlyInMongo.contains(e))

  println("\ninserts acknowledged by mongo = " + insertsAcknowledgedByMongo.size)
  println("currently in mongo = " + currentlyInMongo.length)
  println("elements inserted but no longer found = " + elementsInsertedButNoLongerFound.length)

  sys.exit(0)
}
