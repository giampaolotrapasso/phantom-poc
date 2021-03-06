package com.giampaolotrapasso.phantom

import com.datastax.driver.core.ResultSet
import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.db.DatabaseImpl
import com.giampaolotrapasso.phantom.models._
import com.websudos.phantom.dsl._

//import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._

class Database(val keyspace: KeySpaceDef) extends DatabaseImpl(keyspace) {

  object posts extends PostTable with keyspace.Connector

  object comments extends CommentTable with keyspace.Connector

  object events extends EventTable with keyspace.Connector

  object postByAuthor extends PostByAuthorTable with keyspace.Connector

  def insertCommentBatch(post: Post, comment: Comment) = {
    Batch.logged
      .add(BlogDatabase.posts.insertNewStatement(post))
      .add(BlogDatabase.comments.insertNewStatement(comment))
      .future()
  }

  def insertPost(post: Post): Future[ResultSet] = {
    val event = Event(postId = post.id, timestamp = post.timestamp, eventType = "Insert")

    val postByAuthor = PostByAuthor(
      postId = post.id,
      title = post.title,
      author = post.author,
      timestamp = post.timestamp
    )

    val batch = Batch.logged
      .add(this.postByAuthor.insertNewStatement(postByAuthor))
      .add(posts.insertNewStatement(post))
      .add(events.insertNewStatement(event))

    batch.future
  }

  def selectByAuthor(author: String, limit: Int): Future[List[PostByAuthor]] = {
    postByAuthor.select.where(_.author eqs author).limit(limit).fetch()
  }

  def selectEventsWithFiltering(limit: Int) = {
    events.select.allowFiltering().limit(limit).fetch()
  }

}

object BlogDatabase extends Database(Config.keySpaceDefinition)
