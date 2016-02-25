package deadpool.rest

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
import deadpool.models.{ActionThreadsEnum, DeadPoolUsers, Users}


/**
  * Created by thiago on 2/23/16.
  */
trait UsersService extends HttpService {

  implicit val timeout = Timeout(5 seconds)
  implicit def executionContext = actorRefFactory.dispatcher
  val errorUser = "{\"status\": \"error\"}"
  val saveUser = (put | post)
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
          val userQuery = Users.getById(id.toLong)
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
          }        } ~
        delete {
          complete("USERS DEL")
        }

      }

      path("users" / Segment / "state") { username =>
        get {
          val userQuery = Users.getByUsername(username)
          onComplete(userQuery) {
            case Success(some: List[DeadPoolUsers]) =>
              if(!some.isEmpty)
                complete(some.head.myThreads.get(ActionThreadsEnum.REPLY).get)
              else
                complete(errorUser)
            case Failure(error) =>
              println(error.getMessage)
              complete(error)
          }

        }
      }
    }
  }
}
