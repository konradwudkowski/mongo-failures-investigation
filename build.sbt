name := """mongo-write-concerns"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.12.1",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

trapExit := false

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

