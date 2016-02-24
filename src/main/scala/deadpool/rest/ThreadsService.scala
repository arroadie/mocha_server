package deadpool.rest

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http.HttpHeaders._
import spray.json._
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService

import deadpool.rest.formats.RestJsonFormats._
import deadpool.models.{DeadPoolThreads, Threads}

/**
  * Created by thiago on 2/23/16.
  */
trait ThreadsService extends HttpService {

  val error = "{\"status\": \"error\"}"

  val threadsRoutes = {
    respondWithMediaType(`application/json`){
      path("threads" / Segment) { id =>
        get {
          val bla = Threads.getById(id.toLong)
          onComplete(bla) {
            case Success(some: List[DeadPoolThreads]) =>
              if(!some.isEmpty)
                complete(some.head)
              else
                complete(error)
            case Failure(error) =>
              println(error.getMessage)
              complete(error)
          }
        } ~
        put {
          entity(as[DeadPoolThreads]) { thread =>
            Threads.save(thread)
            complete("{\"status\":\"OK\"}")
          }
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
