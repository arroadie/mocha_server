package deadpool.models

import org.bson.Document

import scala.concurrent.Future

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
  * Created by thiago on 2/23/16.
  */
case class DeadPoolThreads(id: Long, parentId: Long, hasChildren: Boolean, userId: Long, username: String, timestamp: Long, message: String) extends Document

object Threads {
  //new DeadPoolThreads(1, 1, false, 1, "dude", 1, "hey")

  val log = Logger(LoggerFactory.getLogger(this.getClass))

  private val collection = Mongo.collection("threads")

  def getById(id: Long): Future[Seq[scala.Document]] = {
    collection.find(and(com.mongodb.client.model.Filters.eq("thread.id", id))).first().toFuture()

  }

  def save(thread: DeadPoolThreads): Boolean = {
    val doc = scala.Document(
      "thread" -> scala.Document(
        "id"->thread.id,
        "parentId"->thread.parentId,
        "hasChildren"->thread.hasChildren,
        "userId" -> thread.userId,
        "username" -> thread.username,
        "timestamp" -> thread.timestamp,
        "message" -> thread.message
          )
      )
    collection.insertOne(doc).toFuture().isCompleted
  }

}

object ActionThreadsEnum extends Enumeration {
  val FAVORITE = Value("favorite")
  val PINNED = Value("pinned")
  val REPLY = Value("reply")
  val CREATED = Value("created")
  val IGNORED = Value("ignored")
}