package rest

import spray.http.MediaTypes._
import spray.routing.HttpService

/**
  * Created by thiago on 2/23/16.
  */
trait UsersController extends HttpService {

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
