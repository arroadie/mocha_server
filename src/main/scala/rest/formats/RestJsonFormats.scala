package deadpool.rest.formats

import deadpool.models.DeadPoolThreads
import spray.json._

/**
  * Created by thiago on 2/23/16.
  */
object RestJsonFormats extends DefaultJsonProtocol {

  implicit val threadsJsonFormat = jsonFormat7(DeadPoolThreads)

}
