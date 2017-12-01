package nl.knaw.dans.easy.download.components

import java.net.{ URI, URLEncoder }
import java.nio.file.Path
import java.util.UUID

import nl.knaw.dans.easy.download.HttpStatusException
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.DefaultFormats

import scala.util.{ Failure, Success, Try }
import scalaj.http.{ Http, HttpResponse }

trait AuthInfoComponent extends DebugEnhancedLogging {
  val authInfo: AuthInfo
  implicit val jsonFormats: DefaultFormats.type = DefaultFormats

  trait AuthInfo {
    val baseUri: URI

    def getOutInfo(bagId: UUID, path: Path): Try[FileItemAuthInfo] = {
      for {
        f <- Try(URLEncoder.encode(path.toString, "UTF8"))
        uri = baseUri.resolve(s"$bagId/$f")
        response = Http(uri.toString).method("GET").asString
        _ <- validateResponseCode(uri, response)
      } yield parse(response.body).extract[FileItemAuthInfo]
    }

    private def validateResponseCode(uri: URI, response: HttpResponse[String]) = {
      if (response.code == OK_200) Success(())
      else Failure(HttpStatusException(s"Could not get AUTH-INFO for $uri", HttpResponse(response.statusLine, response.code, response.headers)))
    }
  }
}
