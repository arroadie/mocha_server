package deadpool.rest

import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.MediaTypes._
import spray.routing.HttpService

/**
  * Created by thiago on 2/23/16.
  */
trait UsersController extends HttpService {

  implicit val timeout = Timeout(5 seconds)
  implicit def executionContext = actorRefFactory.dispatcher

  val usersRoutes = {
    path("users" /  Segment ) { id =>
      respondWithMediaType(`application/json`) {
        get {
          complete("USERS GET:" + id)
        } ~
        put {
          complete("USERS PUT")
        } ~
        delete {
          complete("USERS DEL")
        }
      } ~
      path("users" / Segment) { id =>
        get {
          complete(s"looking for user: $id")
        }
      }
    } ~
    path("users") {
      respondWithMediaType(`application/json`){
          post {
            complete("METHOD NOT IMPLEMENTED")
          }
      }
    }
  }
}
