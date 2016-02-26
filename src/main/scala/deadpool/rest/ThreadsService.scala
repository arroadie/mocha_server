package deadpool.rest

import scala.concurrent.Await
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
import deadpool.models.{ThreadsResponse, DeadPoolThreads, Threads}

/**
  * Created by thiago on 2/23/16.
  */
trait ThreadsService extends HttpService {

  val error = "{\"status\": \"error\"}"
  val save = (put | post)

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
                complete(404, error)
            case Failure(error) =>
              println(error.getMessage)
              complete(500, error)
          }
        } ~
        save {
          entity(as[DeadPoolThreads]) { thread =>
            complete(Threads.save(thread.copy(parent_id = id.toLong)))
          }
        }
    } ~
        path("threads" / Segment / "children") { id =>
          get {
            val bla = Threads.getByParentId(id.toLong)
            onComplete(bla) {
              case Success(some: List[DeadPoolThreads]) =>
                complete(ThreadsResponse(id.toLong, Await.result(Threads.getById(id.toLong), 1 second).head.message, some))
              case Failure(error) =>
                println(error.getMessage)
                complete(error)
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
      } ~
      path("threads" / Segment / "join") { name =>
        dynamic {
          get {
            complete(Threads.getByName(name))
          }
        }
      }
    }
  }

}
