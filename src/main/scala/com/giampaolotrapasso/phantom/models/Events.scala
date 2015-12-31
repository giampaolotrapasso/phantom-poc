package com.giampaolotrapasso.phantom.models

import java.util.UUID

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.{ DateTimeColumn, SetColumn, PrimitiveColumn }
import com.websudos.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

case class Event(postId: UUID, eventType: String, timestamp: DateTime)

sealed class EventColumnFamily extends CassandraTable[EventColumnFamily, Event] {

  object postId extends TimeUUIDColumn(this) with PartitionKey[UUID]

  object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending

  object eventType extends StringColumn(this) with ClusteringOrder[String]

  override def fromRow(row: Row): Event = {
    Event(
      postId = postId(row),
      timestamp = timestamp(row),
      eventType = eventType(row)
    )
  }
}

abstract class EventTable extends EventColumnFamily with RootConnector {

  override val tableName = "events"

  def insertNewStatement(event: Event) = {
    insert
      .value(_.postId, event.postId)
      .value(_.eventType, event.eventType)
      .value(_.timestamp, event.timestamp)
  }

  def selectByPostId(postId: UUID) = {
    select.where(_.postId eqs postId).fetch()
  }

}

