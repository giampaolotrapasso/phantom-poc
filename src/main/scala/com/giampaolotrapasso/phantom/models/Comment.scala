package com.giampaolotrapasso.phantom.models

import java.util.UUID

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.{ DateTimeColumn, SetColumn, PrimitiveColumn }
import com.websudos.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

case class Comment(
  id: UUID,
  postId: UUID,
  text: String,
  author: String
)

sealed class CommentColumnFamily extends CassandraTable[CommentColumnFamily, Comment] {

  object postId extends TimeUUIDColumn(this) with PartitionKey[UUID]

  object id extends TimeUUIDColumn(this) with ClusteringOrder[UUID] with Descending

  object text extends StringColumn(this)

  object author extends StringColumn(this)

  override def fromRow(row: Row): Comment = {
    Comment(
      id = id(row),
      postId = postId(row),
      text = text(row),
      author = author(row)
    )
  }

}

abstract class CommentTable extends CommentColumnFamily with RootConnector {

  override val tableName = "comments"

  def insertNewStatement(comment: Comment) = {
    insert
      .value(_.id, comment.id)
      .value(_.postId, comment.postId)
      .value(_.author, comment.author)
      .value(_.text, comment.text)
  }

  def insertNew(comment: Comment): Future[ResultSet] = insertNewStatement(comment).future()

  def selectByPost(postId: UUID): Future[Option[Comment]] = {
    select.where(_.postId eqs postId).one()
  }

  def updateAuthor(postId: UUID, id: UUID, author: String): Future[ResultSet] = {
    update.where(_.postId eqs postId).and(_.id eqs id).modify(_.author setTo author).future()
  }

}

