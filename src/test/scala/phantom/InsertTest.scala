package phantom

import java.util.UUID

import com.websudos.phantom.connectors.KeySpace
import com.giampaolotrapasso.phantom.models.Post
import org.joda.time.DateTime

import collection.mutable.Stack
import org.scalatest._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class InsertSpec extends TestSuite {

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

    TestBlogDatabase.insertPost(post)

    val eventualMaybePost: Future[Option[Post]] = TestBlogDatabase.posts.selectById(post.id)

    val maybePost = eventualMaybePost.futureValue
    maybePost.isDefined should equal(true)
    maybePost.get.id should equal(post.id)
    maybePost.get.author should equal(post.author)

  }

  it should "throw NoSuchElementException if an empty stack is popped" in {

  }

}