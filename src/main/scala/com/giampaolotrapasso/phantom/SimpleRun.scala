package com.giampaolotrapasso.phantom

import com.datastax.driver.core.Session
import com.giampaolotrapasso.phantom.models.{ Post, PostByAuthor }
import org.apache.cassandra.utils.UUIDGen
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.util.{ Try, Failure, Success }

object SimpleRun extends App {

  def doStuff(implicit session: Session) = {

    implicit val space = BlogDatabase.space

    Await.result(BlogDatabase.autocreate().future, 10 seconds)

    val timestamp = DateTime.now

    val post1 = Post(
      id = UUIDGen.getTimeUUID, title = "Hello world",
      author = "Brian W. Kernighan",
      text = """The only way to learn a new programming language is by writing programs
               |in it. The first program to write is the same for all languages:
               |Print the words
               |hello, world""",
      tags = Set("C", "programming"),
      timestamp = DateTime.now()
    )

    val post2 = Post(
      id = UUIDGen.getTimeUUID(),
      title = "Pimp my library",
      author = "Martin Odersky",
      text =
        """There's a fundamental difference between your own code and libraries of other people:
          |You can change or extend your own code, but if you want to use some
          |other libraries you have to take them as they are.""".stripMargin,
      tags = Set("Scala", "implicits"),
      timestamp = new DateTime(2006, 10, 9, 0, 0, 0)
    )

    val post3 = Post(
      id = UUIDGen.getTimeUUID,
      title = "Erik Meijer: AGILE must be destroyed, once and for all",
      author = "Tim Anderson",
      text = """A couple of months back, Dutch computer scientist Erik Meijer gave an outspoken and
           distinctly anti-Agile talk at the Reaktor Dev Day in Finland.
               |
               |“Agile is a cancer that we have to eliminate from the industry,"
               |said Meijer; harsh words for a methodology that started in the nineties as a
               |lightweight alternative to bureaucratic and inflexible approaches to software development.""",
      timestamp = new DateTime(2015, 1, 8, 0, 0, 0),
      tags = Set("agile", "flames")
    )

    val post4 = Post(
      id = UUIDGen.getTimeUUID,
      title = "Scala for-comprehension with concurrently running futures",
      author = "Rado Buranský",
      text =
      """Can you tell what’s the difference between the following two?
          |If yes, then you’re great and you don’t need to read further""".stripMargin,
      timestamp = new DateTime(2014, 5, 12, 0, 0, 0),
      tags = Set("scala", "futures")
    )

    val post5 = Post(
      id = UUIDGen.getTimeUUID,
      title = "The Myth Makers 1: Scala's \"Type Types",
      author = "Martin Odersky",
      text =
      """2008 has seen a lot of activity around Scala. All major IDEs now have working Scala plugins.
          |A complete Scala tutorial and reference book was published and several others are in the pipeline.
          | Scala is used in popular environments and frameworks,
          | and is being adopted by more and more professional programmers in organizations
          | like Twitter, Sony Imageworks, and Nature, along with many others.""".stripMargin,
      timestamp = new DateTime(2008, 12, 18, 0, 0, 0),
      tags = Set("scala", "types")
    )

    val posts = List(post1, post2, post3, post4, post5).map(p => BlogDatabase.insertPost(p))

    val operations = Future.sequence(posts)

    Await.result(operations, 10.seconds)

    val listOfOdersky: Future[List[PostByAuthor]] = BlogDatabase.selectByAuthor("Martin Odersky", 10)

    listOfOdersky.onComplete {
      case Success(list) => println(s"${list.size} posts by Odersky ")
      case Failure(x) => println(x)
    }

    Await.result(listOfOdersky, 10.seconds)

    val events = BlogDatabase.selectEventsWithFiltering(5)

    events.onComplete {
      case Success(list) => println(s"${list.size} events")
      case Failure(x) => println(x)
    }

    Await.ready(events, 3.seconds)

    println("Sample ended")
    System.exit(0)
  }

  /*
    to start a C* cluster do
    brew install ccm
    sudo ifconfig lo0 alias 127.0.0.2
    sudo ifconfig lo0 alias 127.0.0.3
    ccm create -v 2.1.5 -n 3 clusterTest
    ccm start
  */

  val connect: Try[Session] = Try(BlogDatabase.session)

  connect match {
    case Success(session) => doStuff(session)
    case Failure(error) => println(s"KO: ${error}")
  }

}
