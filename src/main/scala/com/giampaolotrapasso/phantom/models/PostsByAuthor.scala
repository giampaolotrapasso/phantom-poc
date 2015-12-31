package com.giampaolotrapasso.phantom.models

import java.util.UUID

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.Unspecified
import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.column.{ DateTimeColumn, SetColumn, PrimitiveColumn }
import com.websudos.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

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

  def insertNewStatement(post: PostByAuthor): InsertQuery[PostByAuthorColumnFamily, PostByAuthor, Unspecified] = {
    insert
      .value(_.postId, post.postId)
      .value(_.title, post.title)
      .value(_.author, post.author)
      .value(_.timestamp, post.timestamp)
  }

  def insertNew(postByAuthor: PostByAuthor) = insertNewStatement(postByAuthor).future

}

