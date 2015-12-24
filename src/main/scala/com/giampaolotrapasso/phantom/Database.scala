package com.giampaolotrapasso.phantom

import com.datastax.driver.core.ResultSet
import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.db.DatabaseImpl
import com.giampaolotrapasso.phantom.models._
import com.websudos.phantom.dsl.Batch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Database(val keyspace: KeySpaceDef) extends DatabaseImpl(keyspace) {

  object posts extends PostTable with keyspace.Connector

  object comments extends CommentTable with keyspace.Connector

  object events extends EventTable with keyspace.Connector

  object postByAuthor extends PostByAuthorTable with keyspace.Connector

  def updateSchema = {
    for {
      p <- posts.create.ifNotExists().future()
      c <- comments.create.ifNotExists().future()
      e <- events.create.ifNotExists().future()
      pba <- postByAuthor.create.ifNotExists().future()
    } yield (p, c, e, pba)
  }

  def insertPostBatch(post: Post, event: Event) = {

    val postByAuthor = PostByAuthor(
      postId = post.id,
      title = post.title,
      author = post.author,
      timestamp = post.timestamp
    )

    Batch.logged
      .add(BlogDatabase.postByAuthor.insertNewStatement(postByAuthor))
      .add(BlogDatabase.posts.insertNewStatement(post))
      .add(BlogDatabase.events.insertNewStatement(event))
      .future()
  }

  def insertCommentBatch(post: Post, comment: Comment) = {
    Batch.logged
      .add(BlogDatabase.posts.insertNewStatement(post))
      .add(BlogDatabase.comments.insertNewStatement(comment))
      .future()
  }

  def insertPost(post: Post) = {
    val event = Event(postId = post.id, timestamp = post.timestamp, eventType = "Insert")

    val postByAuthor = PostByAuthor(
      postId = post.id,
      title = post.title,
      author = post.author,
      timestamp = post.timestamp
    )

    Batch.logged
      .add(BlogDatabase.postByAuthor.insertNewStatement(postByAuthor))
      .add(BlogDatabase.posts.insertNewStatement(post))
      .add(BlogDatabase.events.insertNewStatement(event))
      .future()
  }

}

object BlogDatabase extends Database(Config.keySpaceDefinition)
