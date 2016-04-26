package com.giampaolotrapasso.phantom.models

import java.util.UUID

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import org.joda.time.DateTime

case class PostByAuthor(
  author: String,
  timestamp: DateTime,
  postId: UUID,
  title: String
)

sealed class PostByAuthorColumnFamily extends CassandraTable[PostByAuthorColumnFamily, PostByAuthor] {

  object author extends StringColumn(this) with PartitionKey[String]

  object postId extends TimeUUIDColumn(this) with ClusteringOrder[UUID] with Descending

  object title extends StringColumn(this) with ClusteringOrder[String]

  object timestamp extends DateTimeColumn(this)

  override def fromRow(row: Row): PostByAuthor = {
    PostByAuthor(
      postId = postId(row),
      title = title(row),
      author = author(row),
      timestamp = timestamp(row)
    )
  }
}

abstract class PostByAuthorTable extends PostByAuthorColumnFamily with RootConnector {

  override val tableName = "posts_by_author"

  def insertNewStatement(post: PostByAuthor) = {
    insert
      .value(_.postId, post.postId)
      .value(_.title, post.title)
      .value(_.author, post.author)
      .value(_.timestamp, post.timestamp)
  }

  def insertNew(postByAuthor: PostByAuthor) = insertNewStatement(postByAuthor).future

}

