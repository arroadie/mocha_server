package deadpool.models


import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.typesafe.scalalogging.Logger

import com.mongodb.async.client.{Subscription, Observer}
import org.mongodb.scala
import org.mongodb.scala.bson.{BsonString, BsonArray, BsonNumber, BsonBoolean}
import org.mongodb.scala.{Completed, Observable, MongoCollection, ScalaObservable}
import org.mongodb.scala.bson.conversions.Bson
import com.mongodb.client.model.Filters._
import com.mongodb.client.model.Updates._
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq

import org.slf4j.LoggerFactory

import deadpool.sources.Mongo

/**
  * Chat threads
  */
case class DeadPoolThreads(id: Option[Long], parent_id: Long, has_children: Option[Boolean], user_id: Option[Long], user_name: String, timestamp: Option[Long], message: String, pinned: Option[Boolean])

case class ThreadsResponse(id: Long, message: String, children: List[DeadPoolThreads])

object Threads {

  //new DeadPoolThreads(1, 1, false, 1, "dude", 1, "hey")

  val filters = com.mongodb.client.model.Filters
  val log = Logger(LoggerFactory.getLogger(this.getClass))

  private val collection = Mongo.collection("threads")

  def getById(id: Long): Future[Seq[DeadPoolThreads]] =
    collection.find(and(filters.eq("thread.id", id))).first().toFuture.map[Seq[DeadPoolThreads]]{x => x.map { x =>
      val thread = x.get("thread").get
      DeadPoolThreads(
        Some(thread.asDocument().get("id").asNumber().longValue()),
        thread.asDocument().get("parent_id").asNumber().longValue(),
        Some(thread.asDocument().get("has_children").asBoolean().getValue),
        Some(thread.asDocument().get("user_id").asNumber().longValue()),
        thread.asDocument().get("user_name").asString().getValue,
        Some(thread.asDocument().get("timestamp").asNumber().longValue()),
        thread.asDocument().get("message").asString().getValue,
        Some(thread.asDocument().getOrDefault("pinned", BsonBoolean(false)).asBoolean().getValue)
      )
    }}

  def getById(ids: List[Long]): Future[Seq[DeadPoolThreads]] = {
    Future(ids.map{i => Await.result(getById(i), 10 seconds)}.flatten)
  }

  def getByParentId(id: Long): Future[Seq[DeadPoolThreads]] =
    collection.find(and(filters.eq("thread.parent_id", id))).toFuture.map[Seq[DeadPoolThreads]]{x => x.map { x =>
      val thread = x.get("thread").get
      DeadPoolThreads(
        Some(thread.asDocument().get("id").asNumber().longValue()),
        thread.asDocument().get("parent_id").asNumber().longValue(),
        Some(thread.asDocument().get("has_children").asBoolean().getValue),
        Some(thread.asDocument().get("user_id").asNumber().longValue()),
        thread.asDocument().get("user_name").asString().getValue,
        Some(thread.asDocument().get("timestamp").asNumber().longValue()),
        thread.asDocument().get("message").asString().getValue,
        Some(thread.asDocument().getOrDefault("pinned", BsonBoolean(false)).asBoolean().getValue)
      )
    }}

  def save(thread: DeadPoolThreads): DeadPoolThreads = {
    val id = System.nanoTime()
    val savingTime = System.currentTimeMillis()
    val doc = scala.Document(
      "thread" -> scala.Document(
        "id"-> id,
        "parent_id"->thread.parent_id,
        "has_children"->thread.has_children.getOrElse(false),
        "user_id" -> thread.user_id.getOrElse(-1.toLong),
        "user_name" -> thread.user_name,
        "timestamp" -> savingTime,
        "message" -> thread.message,
        "pinned" -> thread.pinned.getOrElse(false)
          )
      )
    collection.insertOne(doc).toFuture().isCompleted

    Future{
      swapParentState(thread.parent_id)
    }

    Future {
      Users.subscribe(thread.user_name, List(thread.parent_id), ActionThreadsEnum.REPLY)
    }

    DeadPoolThreads(
      Some(doc.get("thread").get.asDocument().get("id", BsonNumber(id)).asNumber().longValue()),
      doc.get("thread").get.asDocument().get("parent_id").asNumber().longValue(),
      Some(doc.get("thread").get.asDocument().get("has_children").asBoolean().getValue),
      Some(doc.get("thread").get.asDocument().get("user_id").asNumber().longValue()),
      doc.get("thread").get.asDocument().get("user_name").asString().getValue,
      Some(doc.get("thread").get.asDocument().get("timestamp", BsonNumber(savingTime)).asNumber().longValue()),
      doc.get("thread").get.asDocument().get("message").asString().getValue,
      Some(doc.get("thread").get.asDocument().getOrDefault("pinned", BsonBoolean(false)).asBoolean().getValue)
    )
  }

  def getByName(name: String): Future[Seq[DeadPoolThreads]] = {
    collection.find(and(filters.eq("thread.message", name), filters.eq("thread.parent_id", -1))).toFuture.map[Seq[DeadPoolThreads]]{x => x.map { x =>
      val thread = x.get("thread").get
      DeadPoolThreads(
        Some(thread.asDocument().get("id").asNumber().longValue()),
        thread.asDocument().get("parent_id").asNumber().longValue(),
        Some(thread.asDocument().get("has_children").asBoolean().getValue),
        Some(thread.asDocument().get("user_id").asNumber().longValue()),
        thread.asDocument().get("user_name").asString().getValue,
        Some(thread.asDocument().get("timestamp").asNumber().longValue()),
        thread.asDocument().get("message").asString().getValue,
        Some(thread.asDocument().getOrDefault("pinned", BsonBoolean(false)).asBoolean().getValue)
      )
    }}
  }

  def swapParentState(id: Long): Unit = {
    collection.updateOne(filters.eq("thread.id", id), set("thread.has_children", true)).toFuture().isCompleted
  }

  def addTag(id: Long, tag: String): Thread = {
    Await.result(
      collection.updateOne(
        and(
          com.mongodb.client.model.Filters.eq("thread.id", id)),
        com.mongodb.client.model.Updates.addToSet("thread.tags", BsonString(tag)))
        .toFuture(), 10 seconds).andThen[Thread]{updated => Await.result(collection.find(and(filters.eq("_id", updated.getUpsertedId))).toFuture(), 10 seconds).asInstanceOf[Thread]}.lift
  }
}

object ActionThreadsEnum extends Enumeration {
  val FAVORITE = Value("favorite")
  val PINNED = Value("pinned")
  val REPLY = Value("reply")
  val CREATED = Value("created")
  val IGNORED = Value("ignored")
}