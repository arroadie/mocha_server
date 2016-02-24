package deadpool.rest

import org.bson.Document
import spray.http.MediaTypes
import spray.http.MediaTypes._
import akka.actor.Actor
import akka.util.Timeout
import spray.httpx.unmarshalling.Unmarshaller
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

  val error = "{\"status\": \"error\"}"

  val threadsRoutes = {
    respondWithMediaType(`application/json`){
      path("threads" / Segment) { id =>
        get {
          val bla = Threads.getById(id.toLong)
          onComplete(bla) {
            case Success(some: List[org.mongodb.scala.Document]) =>
              if(some.nonEmpty && some.head.get("thread").isDefined)
                complete(some.head.get("thread").get.toString)
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
