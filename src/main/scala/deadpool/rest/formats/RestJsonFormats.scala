package deadpool.rest.formats

import deadpool.models.{ThreadsResponse, ActionThreadsEnum, DeadPoolUsers, DeadPoolThreads}
import spray.json._

/**
  * Created by thiago on 2/23/16.
  */
object RestJsonFormats extends DefaultJsonProtocol {


  implicit object actionThreadsJsonFormat extends RootJsonFormat[ActionThreadsEnum.Value] {
    def write(obj: ActionThreadsEnum.Value): JsValue = JsString(obj.toString)

    def read(json: JsValue): ActionThreadsEnum.Value = json match {
      case JsString(str) => ActionThreadsEnum.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }

  implicit val threadsJsonFormat = jsonFormat7(DeadPoolThreads.apply)
  implicit val usersJsonFormat   = jsonFormat3(DeadPoolUsers.apply)
  implicit val threadsResponseFormat = jsonFormat2(ThreadsResponse)

}
