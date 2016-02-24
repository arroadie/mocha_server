name := "deadpool_server"

version := "1.0"

scalaVersion := "2.11.7"

seq(Twirl.settings: _*)

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.2"
libraryDependencies += "io.spray" % "spray-can_2.11" % "1.3.3"
libraryDependencies += "io.spray" % "spray-routing_2.11" % "1.3.3"
libraryDependencies += "io.spray" % "spray-client_2.11" % "1.3.3"
libraryDependencies += "io.spray" % "spray-json_2.10" % "1.3.2"
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.0"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
