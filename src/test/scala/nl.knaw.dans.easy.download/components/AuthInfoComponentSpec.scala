package nl.knaw.dans.easy.download.components

import java.net.URI
import java.nio.file.{ Path, Paths }
import java.util.UUID

import nl.knaw.dans.easy.download.{ Configuration, EasyDownloadApp, TestSupportFixture }
import org.apache.commons.configuration.PropertiesConfiguration
import org.scalamock.scalatest.MockFactory

import scala.util.{ Failure, Success }

class AuthInfoComponentSpec extends TestSupportFixture with MockFactory {
  private val app = new EasyDownloadApp {
    override val http: HttpWorker = mock[HttpWorker]
    override lazy val configuration: Configuration = new Configuration("", new PropertiesConfiguration() {
      addProperty("bag-store.url", "http://localhost:20110/")
      addProperty("auth-info.url", "http://localhost:20170/")
    })
  }
  private val uuid = UUID.randomUUID()

  private def expectAutInfo(path: Path) = {
    (app.http.getHttpAsString(_: URI)) expects new URI(s"http://localhost:20170/$uuid/$path") once()
  }

  "getOutInfo" should "parse the service response" in {
    val path = Paths.get("some.file")
    expectAutInfo(path) returning Success(
      s"""{
         |  "itemId":"$uuid/some.file",
         |  "owner":"someone",
         |  "dateAvailable":"1992-07-30",
         |  "accessibleTo":"KNOWN",
         |  "visibleTo":"ANONYMOUS"
         |}""".stripMargin
    )
    app.authInfo.getOutInfo(uuid, path) shouldBe a[Success[_]]
  }

  it should "complain about invalid service response" in {
    val path = Paths.get("some.file")
    expectAutInfo(path) returning Success(
      s"""{"nonsense":"value"}"""
    )
    inside(app.authInfo.getOutInfo(uuid, path)) {
      case Failure(t) => t should have message
        """parse error [No usable value for itemId
          |Did not find value which can be converted into java.lang.String] for: {"nonsense":"value"}""".stripMargin
    }
  }

  it should "stumble over invalid accessibleTo" in {
    val path = Paths.get("some.file")
    expectAutInfo(path) returning Success(
      s"""{
         |  "itemId":"$uuid/some.file",
         |  "owner":"someone",
         |  "dateAvailable":"1992-07-30",
         |  "accessibleTo":"invaidValue",
         |  "visibleTo":"ANONYMOUS"
         |}""".stripMargin
    )
    inside(app.authInfo.getOutInfo(uuid, path)) {
      case Failure(t) =>
        println(t.getMessage)
        t.getMessage shouldBe // "[unknown error]: see todo in FileItemAuthInfo"
          s"""parse error [unknown error] for: {
            |  "itemId":"$uuid/some.file",
            |  "owner":"someone",
            |  "dateAvailable":"1992-07-30",
            |  "accessibleTo":"invaidValue",
            |  "visibleTo":"ANONYMOUS"
            |}""".stripMargin
    }
  }
}
