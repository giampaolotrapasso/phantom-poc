package com.giampaolotrapasso.phantom

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.datastax.driver.core.{ ResultSetFuture, ResultSet }

import com.giampaolotrapasso.phantom.models.{ Event, Post, PostTable }
import org.joda.time.DateTime

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

import scala.util.{ Try, Success, Failure }

import scala.concurrent.ExecutionContext.Implicits.global

object SimpleRun extends App {

  /*
    to start a C* cluster do
    brew install ccm
    sudo ifconfig lo0 alias 127.0.0.2
    sudo ifconfig lo0 alias 127.0.0.3
    ccm create -v 2.1.5 -n 3 clusterTest
    ccm start
  */
  implicit val session = BlogDatabase.session
  implicit val space = BlogDatabase.space

  Await.result(BlogDatabase.autocreate().future, 10 seconds)

  val post = Post(
    id = UUID.randomUUID(), title = "Hello world",
    author = "Brian W. Kernighan",
    text = """The only way to learn a new programming language is by writing programs
             |in it. The first program to write is the same for all languages:
             |Print the words
             |hello, world""",
    tags = Set("C", "programming"),
    timestamp = DateTime.now()
  )

  val futureResultSet = BlogDatabase.posts.insertNew(post)

  futureResultSet onComplete {
    case Success(resultSet) => resultSet.all().foreach(println)
    case Failure(t) => println("An error has occurred: " + t.getMessage)
  }

  val timestamp = DateTime.now

  val otherPost = Post(
    id = UUID.randomUUID(),
    title = "Pimp my library",
    author = "Odersky",
    text =
      """There's a fundamental difference between your own code and libraries of other people:
        |You can change or extend your own code, but if you want to use some
        |other libraries you have to take them as they are.""".stripMargin,
    tags = Set("Scala", "implicits"),
    timestamp = timestamp
  )

  // something batchy
  val event = Event(postId = otherPost.id, timestamp = timestamp, eventType = "Insert")

  val futureBatch = BlogDatabase.insertPostBatch(otherPost, event)

  futureBatch onComplete {
    case Success(resultSet) => {
      println("Batch OK")
      resultSet.all().foreach(println)
    }
    case Failure(t) => println("An error has occurred on batch: " + t.getMessage)
  }

  val timestamp2 = DateTime.now

  val thirdPost = Post(
    id = UUID.randomUUID(),
    title = "Erik Meijer: AGILE must be destroyed, once and for all",
    author = "Tim Anderson",
    text = """""",
    timestamp = timestamp2,
    tags = Set("agile", "flames", "rant")
  )

  val insertBatch = BlogDatabase.insertPost(otherPost)

  val operations = for {
    a <- futureResultSet
    b <- futureBatch
    c <- insertBatch
  } yield ()

  Await.result(operations, 10 seconds)

}
