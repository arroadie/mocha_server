package deadpool.models

import com.mongodb.async.client.{Subscription, Observer}
import com.typesafe.scalalogging.Logger
import org.bson.Document
import org.mongodb.scala
import org.mongodb.scala.{Completed, Observable, MongoCollection, ScalaObservable}
import org.mongodb.scala.bson.conversions.Bson
import org.slf4j.LoggerFactory
import sources.Mongo
import com.mongodb.client.model.Filters._
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq

import _root_.scala.concurrent.Future

/**
  * Created by thiago on 2/23/16.
  */
case class DeadPoolThreads(id: Long, parentId: Long, hasChildren: Boolean, userId: Long, username: String, timestamp: Long, message: String) extends Document

object Threads {
  //new DeadPoolThreads(1, 1, false, 1, "dude", 1, "hey")

  val log = Logger(LoggerFactory.getLogger(this.getClass))

  private val collection = Mongo.collection("threads")

  def getById[T <: Document](id: String): Future[Seq[scala.Document]] = {

    collection.find(and(com.mongodb.client.model.Filters.eq("id", id))).first().toFuture()

  }

}
