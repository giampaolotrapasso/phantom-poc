package phantom

import java.util.UUID

import com.websudos.phantom.connectors.KeySpace
import com.giampaolotrapasso.phantom.models.Post
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures

import collection.mutable.Stack
import org.scalatest._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class BasicOperationsTest extends TestSuite {

  "A BlogDatabase" should "insert correctly a blog post" in {

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

    val result = for {
      insert <- TestBlogDatabase.insertPost(post)
      select <- TestBlogDatabase.posts.selectById(post.id)
    } yield (insert, select)

    val maybeSelect = result.futureValue._2

    maybeSelect.isDefined should equal(true)
    maybeSelect.get.id should equal(post.id)
    maybeSelect.get.author should equal(post.author)

  }

}