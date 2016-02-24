package deadpool.models

import com.mongodb.client.model.Filters._
import com.typesafe.scalalogging.Logger
import deadpool.sources.Mongo
import org.bson.Document
import org.mongodb.scala
import org.slf4j.LoggerFactory

import _root_.scala.concurrent.Future



/**
 * Created by userti on 24-02-16.
 */

case class DeadPoolUsers(userId: Long, username: String, myThreads: Map[Long, ActionThreadsEnum.Value]) extends Document

object Users {

  //new DeadPoolUsers(1, antonbas, )

  val log = Logger(LoggerFactory.getLogger(this.getClass))

  private val collection = Mongo.collection("users")

  def getById[T <: Document](id: String): Future[Seq[scala.Document]] = {

    collection.find(and(com.mongodb.client.model.Filters.eq("id", id))).first().toFuture()

  }

}
