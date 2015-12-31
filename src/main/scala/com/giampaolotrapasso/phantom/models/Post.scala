package com.giampaolotrapasso.phantom.models

import java.util.UUID

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.Unspecified
import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.column.{ DateTimeColumn, SetColumn }
import com.websudos.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

case class Post(
  id: UUID,
  title: String,
  author: String,
  text: String,
  tags: Set[String],
  timestamp: DateTime
)

sealed class PostColumnFamily extends CassandraTable[PostColumnFamily, Post] {

  object id extends TimeUUIDColumn(this) with PartitionKey[UUID]

  object title extends StringColumn(this) with ClusteringOrder[String]

  object author extends StringColumn(this)

  object text extends StringColumn(this)

  object tags extends SetColumn[PostColumnFamily, Post, String](this)

  object timestamp extends DateTimeColumn(this)

  override def fromRow(row: Row): Post = {
    Post(
      id = id(row),
      title = title(row),
      author = author(row),
      text = text(row),
      tags = tags(row),
      timestamp = timestamp(row)
    )
  }
}

abstract class PostTable extends PostColumnFamily with RootConnector {

  override val tableName = "posts"

  def insertNewStatement(post: Post): InsertQuery[PostColumnFamily, Post, Unspecified] = {
    insert
      .value(_.id, post.id)
      .value(_.title, post.title)
      .value(_.author, post.author)
      .value(_.text, post.text)
      .value(_.tags, post.tags)
      .value(_.timestamp, post.timestamp)
  }

  def insertNew(post: Post) = insertNewStatement(post).future

  def selectById(id: UUID): Future[Option[Post]] = {
    select.where(_.id eqs id).one()
  }

}

