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
  * Chat threads
  */
case class DeadPoolThreads(id: Long, parentId: Long, hasChildren: Boolean, userId: Long, username: String, timestamp: Long, message: String)

object Threads {
  //new DeadPoolThreads(1, 1, false, 1, "dude", 1, "hey")

  val log = Logger(LoggerFactory.getLogger(this.getClass))

  private val collection = Mongo.collection("threads")

  def getById(id: Long): Future[Seq[DeadPoolThreads]] =
    collection.find(and(com.mongodb.client.model.Filters.eq("thread.id", id))).first().toFuture.map[Seq[DeadPoolThreads]]{x => x.map { x =>
      val thread = x.get("thread").get
      DeadPoolThreads(
        thread.asDocument().get("id").asNumber().longValue(),
        thread.asDocument().get("parentId").asNumber().longValue(),
        thread.asDocument().get("hasChildren").asBoolean().getValue,
        thread.asDocument().get("userId").asNumber().longValue(),
        thread.asDocument().get("username").asString().getValue,
        thread.asDocument().get("timestamp").asNumber().longValue(),
        thread.asDocument().get("message").asString().getValue
      )
    }}

  def getByParentId(id: Long): Future[Seq[DeadPoolThreads]] =
    collection.find(and(com.mongodb.client.model.Filters.eq("thread.parentId", id))).toFuture.map[Seq[DeadPoolThreads]]{x => x.map { x =>
      val thread = x.get("thread").get
      DeadPoolThreads(
        thread.asDocument().get("id").asNumber().longValue(),
        thread.asDocument().get("parentId").asNumber().longValue(),
        thread.asDocument().get("hasChildren").asBoolean().getValue,
        thread.asDocument().get("userId").asNumber().longValue(),
        thread.asDocument().get("username").asString().getValue,
        thread.asDocument().get("timestamp").asNumber().longValue(),
        thread.asDocument().get("message").asString().getValue
      )
    }}

  def save(thread: DeadPoolThreads): Long = {
    val id = System.nanoTime()
    val doc = scala.Document(
      "thread" -> scala.Document(
        "id"-> id,
        "parentId"->thread.parentId,
        "hasChildren"->thread.hasChildren,
        "userId" -> thread.userId,
        "username" -> thread.username,
        "timestamp" -> System.currentTimeMillis(),
        "message" -> thread.message
          )
      )
    collection.insertOne(doc).toFuture().isCompleted
    id
  }
}

object ActionThreadsEnum extends Enumeration {
  val FAVORITE = Value("favorite")
  val PINNED = Value("pinned")
  val REPLY = Value("reply")
  val CREATED = Value("created")
  val IGNORED = Value("ignored")
}