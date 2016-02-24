package deadpool.sources

import com.mongodb.async.client.MongoClientSettings
import org.mongodb.scala.MongoClient

/**
  * Created by thiago on 2/23/16.
  */
object Mongo {

  private val client: MongoClient = MongoClient("mongodb://127.0.0.1:27017")

  val db = client.getDatabase("deadpool")
  def collection(name: String) = db.getCollection(name)

}
