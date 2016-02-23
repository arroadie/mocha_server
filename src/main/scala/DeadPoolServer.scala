package deadpool

import actors.RestActor
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.io.IO
import akka.pattern._
import akka.actor._
import akka.util.Timeout

import spray.can.Http

object DeadPoolServer extends App {

  implicit val timeout = Timeout(5 seconds)

  implicit val system = ActorSystem("DeadPool-Server")

  val rest = system.actorOf(Props(classOf[RestActor]))

  println("CHIMICHANGA");

  IO(Http) ! Http.Bind(rest, "0.0.0.0", 8080)

}
