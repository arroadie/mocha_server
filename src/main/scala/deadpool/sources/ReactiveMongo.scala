package deadpool.sources

import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Mongo access object
  */
object ReactiveMongo {

  private val driver = new MongoDriver
  private val connection = driver.connection(List("localhost"))
  private val db = connection("deadpool")
  def collection[T](collName: String): Collection = db(collName)

}
