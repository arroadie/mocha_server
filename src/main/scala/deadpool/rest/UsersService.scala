package deadpool.rest

import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.concurrent.duration._


import akka.util.Timeout

import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http.HttpHeaders._
import spray.json._
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService

import deadpool.rest.formats.RestJsonFormats._
import deadpool.models._


/**
  * Created by thiago on 2/23/16.
  */
trait UsersService extends HttpService {

  implicit val timeout = Timeout(5 seconds)
  implicit def executionContext = actorRefFactory.dispatcher
  val errorUser = "{\"status\": \"error\"}"
  val saveUser = ( put | post )
  val usersRoutes = {
    respondWithMediaType(`application/json`) {
      path("users") {
        get {
          complete("USERS GET")
        } ~
        post {
          complete("METHOD NOT IMPLEMENTED")
        }
      } ~
      path("users" / Segment) { id =>
        get {
          val userQuery = Users.getByUsername(id)
          onComplete(userQuery) {
            case Success(some: List[DeadPoolUsers]) =>
              if(!some.isEmpty)
                complete(some.head)
              else
                complete(errorUser)
            case Failure(error) =>
              println(error.getMessage)
              complete(error)
          }

        } ~
          saveUser {
          entity(as[DeadPoolUsers]) { user =>
            Users.findOrCreate(user)
            complete("{\"status\":\"OK\"}")
          }
        } ~
          delete {
            complete("USERS DEL")
          }
      } ~
      path("users" / Segment / "state") { username =>
        get {
          val userQuery = Users.getByUsername(username)
          onComplete(userQuery) {
            case Success(some: List[DeadPoolUsers]) =>
              if(!some.isEmpty)
                complete(Await.result(Threads.getById(some.head.myThreads.get.get(ActionThreadsEnum.REPLY).get), 10 seconds).toLis)
              else
                complete(errorUser)
            case Failure(error) =>
              println(error.getMessage)
              complete(error)
          }
        }
      } ~
      path("users" / Segment / "threads" / LongNumber / "subscribe") { (username, threadId) =>
        delete {
          dynamic {
            Users.unsubscribe(username, List(threadId), ActionThreadsEnum.REPLY)
            complete("{\"status\":\"deleted\", \"id\":" + threadId + "}")
          }
        } ~
        put {
          dynamic {
            Users.subscribe(username, List(threadId.toLong), ActionThreadsEnum.REPLY)
            complete(ThreadsResponse(threadId, Await.result(Threads.getById(threadId), 1 second).head.message,  Await.result(Threads.getByParentId(threadId), 1 second).toList))
          }
        }
      } ~
        path("users" / Segment / "threads" / LongNumber / "favorite") { (username, threadId) =>
          delete {
            dynamic {
              Users.unsubscribe(username, List(threadId), ActionThreadsEnum.FAVORITE)
              complete("{\"status\":\"deleted\", \"id\":" + threadId + "}")
            }
          } ~
            put {
              dynamic {
                Users.subscribe(username, List(threadId.toLong), ActionThreadsEnum.FAVORITE)
                complete(ThreadsResponse(threadId, Await.result(Threads.getById(threadId), 1 second).head.message, Await.result(Threads.getByParentId(threadId), 1 second).toList))
              }
            }
        } ~
      path("users" / Segment / "favorite"){ username =>
        get {
          dynamic {
            val res = Users.getFavs(username)
            complete(res)
          }
        }
      }
    }
  }
}
