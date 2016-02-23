package rest

import spray.http.MediaTypes._
import akka.actor.Actor
import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.HttpHeaders.RawHeader
import spray.routing.HttpService

/**
  * Created by thiago on 2/23/16.
  */
trait ThreadsService extends HttpService {

  val threadsRoutes = {
    path("threads") {
      respondWithMediaType(`application/json`){
        get {
          complete("THREADS GET")
        } ~
        put {
          complete("THREADS PUT")
        }
      }
    }
  }

}
