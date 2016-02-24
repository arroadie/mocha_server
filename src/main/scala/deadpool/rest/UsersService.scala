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
import deadpool.models.{DeadPoolUsers, Users}


/**
  * Created by thiago on 2/23/16.
  */
trait UsersService extends HttpService {

  implicit val timeout = Timeout(5 seconds)
  implicit def executionContext = actorRefFactory.dispatcher
  val errorUser = "{\"status\": \"error\"}"
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
            case Success(some: List[org.mongodb.scala.Document]) =>
              if(some.nonEmpty && some.head.get("user").isDefined)
                complete(some.head.get("user").get.toString)
              else
                complete(errorUser)
            case Failure(error) =>
              println(error.getMessage)
              complete(error)
          }

        } ~
        put {
          entity(as[DeadPoolUsers]) { user =>
            Users.save(user)
            complete("{\"status\":\"OK\"}")
          }        } ~
        delete {
          complete("USERS DEL")
        }

      }
    }
  }
}
