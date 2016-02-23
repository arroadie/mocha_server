package rest

import spray.http.MediaTypes._
import spray.routing.HttpService

/**
  * Created by thiago on 2/23/16.
  */
trait UsersService extends HttpService {

  val usersRoutes = {
    path("users") {
      respondWithMediaType(`application/json`){
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
      }
    }
  }

}
