package nl.knaw.dans.easy.download.components

import java.io.{ InputStream, OutputStream }
import java.net.{ URI, URLEncoder }
import java.nio.file.Path
import java.util.UUID

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.io.IOUtils

import scala.util.Try
import scalaj.http.Http

trait AuthInfoComponent extends DebugEnhancedLogging {
  val authInfo: AuthInfo

  trait AuthInfo {
    val baseUri: URI

    def getOutInfo(bagId: UUID, path: Path): Try[Unit] = {
      for {
        f <- Try(URLEncoder.encode(path.toString, "UTF8"))
        uri <- Try(baseUri.resolve(s"bags/$bagId/$f"))
        reponse <- Try(Http(uri.toString).method("GET").execute())
        // TODO convert response.body into FileItemAuthInfo
      } yield ()
    }
  }
}
