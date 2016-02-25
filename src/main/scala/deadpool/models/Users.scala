package deadpool.models

import org.bson.Document
import org.mongodb.scala.bson.{BsonDocument, BsonNumber, BsonArray}

import _root_.scala.collection.mutable
import _root_.scala.collection.mutable.ListBuffer
import _root_.scala.util.{Failure, Success}
import _root_.scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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

case class DeadPoolUsers(id: Option[Long], username: String, myThreads: Option[Map[ActionThreadsEnum.Value, List[Long]]])

object Users {

  //new DeadPoolUsers(1, "antonbas", mutable.HashMap.empty[Long, ActionThreadsEnum.Value])

  val log = Logger(LoggerFactory.getLogger(this.getClass))

  private val collection = Mongo.collection("users")


  def getById(id: Long): Future[Seq[DeadPoolUsers]] =
    collection.find(and(com.mongodb.client.model.Filters.eq("user.id", id))).first().toFuture.map[Seq[DeadPoolUsers]]{ x => x.map { y =>
      val user = y.get("user").get
      val tmp_myThreads: mutable.Map[ActionThreadsEnum.Value, List[Long]] = mutable.Map.empty[ActionThreadsEnum.Value, List[Long] ]
      val keysAction = user.asDocument().get("myThreads").asDocument().keySet().iterator()

      while(keysAction.hasNext()) {
        val key = keysAction.next()
        val listValues = user.asDocument().get("myThreads").asDocument().get(key).asArray().getValues.iterator()
        var elems = ListBuffer.empty[Long]
        while(listValues.hasNext) {
          elems += listValues.next().asNumber().longValue()
        }
        tmp_myThreads(ActionThreadsEnum.withName(key)) = elems.toList
      }

      DeadPoolUsers(
        Some(user.asDocument().get("id").asNumber().longValue()),
        user.asDocument().get("username").asString().toString,
       Some(tmp_myThreads.toMap)
      )
    }}

  def getByUsername(username: String): Future[Seq[DeadPoolUsers]] =
    collection.find(and(com.mongodb.client.model.Filters.eq("user.username", username))).first().toFuture.map[Seq[DeadPoolUsers]]{ x => x.map { y =>
      val user = y.get("user").get

      val myThreads: mutable.Map[ActionThreadsEnum.Value, List[Long]] = mutable.Map.empty[ActionThreadsEnum.Value, List[Long] ]
      val keysAction = user.asDocument().get("myThreads").asDocument().keySet().iterator()

      while(keysAction.hasNext()) {
        val key = keysAction.next()
        val listValues = user.asDocument().get("myThreads").asDocument().get(key).asArray().getValues.iterator()
        var elems = ListBuffer.empty[Long]
        elems
        while(listValues.hasNext) {
          elems += listValues.next().asNumber().longValue()
        }
        myThreads(ActionThreadsEnum.withName(key)) = elems.toList
      }

      DeadPoolUsers(
        Some(user.asDocument().get("id").asNumber().longValue()),
        user.asDocument().get("username").asString().toString,
        Some(myThreads.toMap)
      )
    }}

  def findOrCreate(user: DeadPoolUsers): DeadPoolUsers = {
    val userPreviousSaved = Await.result(getByUsername(user.username), 10 seconds)

    if (userPreviousSaved.isEmpty) {
      val id = System.nanoTime()
      val doc = scala.Document(
        "user" -> scala.Document(
          "id" -> id,
          "username" -> user.username,
          "myThreads" -> scala.Document(ActionThreadsEnum.values.map{x => x.toString -> BsonArray()})
        )
      )

      collection.insertOne(doc).toFuture().isCompleted
    }
    user
  }

  def subscribe(username: String, threads: List[Long], action: ActionThreadsEnum.Value): DeadPoolUsers = {
    val userPreviousSaved = Await.result(getByUsername(username), 10 seconds)

    if (userPreviousSaved.nonEmpty)
      threads foreach {el =>
        collection.updateOne(com.mongodb.client.model.Filters.eq("user.username", username), com.mongodb.client.model.Updates.addToSet(s"user.myThreads.${action.toString}", el)).toFuture().onComplete{
          case Success(any) => println("Success: " + any)
          case Failure(any) => println("Failure: " + any)
        }
      }

    Await.result(getByUsername(username), 10 seconds).head
  }

  def unsubscribe(username: String, threads: List[Long], action: ActionThreadsEnum.Value): DeadPoolUsers = {
    val userPreviousSaved = Await.result(getByUsername(username), 10 seconds)

    if (userPreviousSaved.nonEmpty)
      threads foreach {el =>
        collection.updateOne(com.mongodb.client.model.Filters.eq("user.username", username), com.mongodb.client.model.Updates.pull(s"user.myThreads.${action.toString}", el)).toFuture().isCompleted
      }

    Await.result(getByUsername(username), 10 seconds).head
  }

}
