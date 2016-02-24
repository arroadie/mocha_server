package deadpool.actors

import akka.actor.Actor
import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.HttpHeaders.RawHeader
import spray.http.MediaTypes._
import spray.routing.HttpServiceActor
import rest._

/**
  * Created by thiago on 2/23/16.
  */

class RestActor extends HttpServiceActor
with ThreadsService
with UsersController {

  def receive =  runRoute(
    route ~
    threadsRoutes ~
    usersRoutes
  )

  val route = {
    path("") {
      get {
        respondWithMediaType(`application/json`){
          println("HOLA")
          complete("DEADPOOL")
        }
      }
    }
  }
}
