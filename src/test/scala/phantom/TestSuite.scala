package phantom

import com.websudos.phantom.connectors.{ KeySpace, KeySpaceDef, ContactPoint }
import com.giampaolotrapasso.phantom.Database
import org.scalatest.{ Matchers, FlatSpec, BeforeAndAfterAll, Suite }
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object TestConfig {
  val testConnector: KeySpaceDef = ContactPoint.embedded.keySpace("blogTest")
}

object TestBlogDatabase extends Database(TestConfig.testConnector)

trait TestSuite extends FlatSpec with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with TestConfig.testConnector.Connector {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(TestBlogDatabase.autocreate.future(), 4.seconds)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    Await.result(TestBlogDatabase.autotruncate.future(), 10.seconds)
  }

}
