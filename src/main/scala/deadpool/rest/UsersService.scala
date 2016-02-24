package deadpool.rest

import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.MediaTypes._
import spray.routing.HttpService

/**
  * Created by thiago on 2/23/16.
  */
trait UsersService extends HttpService {

  implicit val timeout = Timeout(5 seconds)
  implicit def executionContext = actorRefFactory.dispatcher

  val usersRoutes = {
    respondWithMediaType(`application/json`) {
      path("users") {
        get {
          complete("USERS GET")
        } ~
        put {
          complete("USERS PUT")
        } ~
        delete {
          complete("USERS DEL")
        } ~
        post {
          complete("METHOD NOT IMPLEMENTED")
        }
      } ~
      path("users" / Segment) { id =>
        get {
          complete(s"looking for user: $id")
        }
      }
    }
  }
}
