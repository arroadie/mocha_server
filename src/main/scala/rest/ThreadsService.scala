package deadpool.rest

import org.bson.Document
import spray.http.MediaTypes._
import akka.actor.Actor
import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.HttpHeaders.RawHeader
import spray.routing.HttpService
import spray.json._
import spray.httpx.SprayJsonSupport._

import deadpool.rest.formats.RestJsonFormats._
import deadpool.models.{DeadPoolThreads, Threads}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

/**
  * Created by thiago on 2/23/16.
  */
trait ThreadsService extends HttpService {

  val threadsRoutes = {
    respondWithMediaType(`application/json`){
      path("threads" / Segment) { id =>
        get {
          val bla = Threads.getById(id)
          onComplete(bla) {
            case Success(some: List[Document]) => complete(some.head.toJson)
            case Failure(error) =>
              println(error.getMessage)
              complete("{\"status\": \"error\"}")
          }
        } ~
        put {
          complete("{\"status\":\"OK\"}")
        }
    } ~
        path("threads") {
        get {
          complete("{\"repsonse\": \"THREADS GET\"}")
        } ~
        put {
          complete("{\"repsonse\": \"THREADS PUT\"}")
        }
      }
    }
  }

}
