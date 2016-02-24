package deadpool

import scala.concurrent.duration._

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import deadpool.actors.RestActor
import spray.can.Http

import scala.io.BufferedSource

/**
  * Created by thiago on 2/24/16.
  */
object DeadPoolServer extends App {

  implicit val timeout = Timeout(5 seconds)

  implicit val system = ActorSystem("DeadPool-Server")

  val rest = system.actorOf(Props(classOf[RestActor]))

  val stream : BufferedSource = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/cool.txt"))

  stream.getLines() foreach { line =>
    println(line)
  }

  IO(Http) ! Http.Bind(rest, "0.0.0.0", 8080)

}
