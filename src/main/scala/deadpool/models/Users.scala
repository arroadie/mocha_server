package deadpool.models

import org.bson.Document

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.mongodb.async.client.{Subscription, Observer}
import com.typesafe.scalalogging.Logger
import org.mongodb.scala
import org.mongodb.scala.{Completed, Observable, MongoCollection, ScalaObservable}
import org.mongodb.scala.bson.conversions.Bson
import org.slf4j.LoggerFactory
import deadpool.sources.Mongo
import com.mongodb.client.model.Filters._
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq



/**
 * Created by userti on 24-02-16.
 */

case class DeadPoolUsers(id: Long, username: String, myThreads: Map[Long, ActionThreadsEnum.Value])

object Users {

  //new DeadPoolUsers(1, "antonbas", mutable.HashMap.empty[Long, ActionThreadsEnum.Value])

  val log = Logger(LoggerFactory.getLogger(this.getClass))

  private val collection = Mongo.collection("users")


  def getById(id: Long): Future[Seq[DeadPoolUsers]] =
    collection.find(and(com.mongodb.client.model.Filters.eq("user.id", id))).first().toFuture.map[Seq[DeadPoolUsers]]{ x => x.map { y =>
      val user = y.get("user").get
      DeadPoolUsers(
        user.asDocument().get("id").asNumber().longValue(),
        user.asDocument().get("username").asString().toString,
        Map.empty[Long, ActionThreadsEnum.Value]
      )
    }}

  def getByUsername(username: String): Future[Seq[DeadPoolUsers]] =
    collection.find(and(com.mongodb.client.model.Filters.eq("user.username", username))).first().toFuture.map[Seq[DeadPoolUsers]]{ x => x.map { y =>
      val user = y.get("user").get
      DeadPoolUsers(
        user.asDocument().get("id").asNumber().longValue(),
        user.asDocument().get("username").asString().toString,
        Map.empty[Long, ActionThreadsEnum.Value]
      )
    }}

  def save(user: DeadPoolUsers): Boolean = {
    val doc = scala.Document(
      "user" -> scala.Document(
        "id" ->user.id,
        "username" -> user.username,
        "myThreads" -> None
      )
    )
    collection.insertOne(doc).toFuture().isCompleted
  }
}
