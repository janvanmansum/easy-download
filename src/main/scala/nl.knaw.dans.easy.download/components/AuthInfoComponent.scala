package nl.knaw.dans.easy.download.components

import java.net.{ URI, URLEncoder }
import java.nio.file.Path
import java.util.UUID

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.{ DefaultFormats, _ }
import org.json4s.native.JsonMethods._

import scala.util.Try

trait AuthInfoComponent extends DebugEnhancedLogging {
  this: HttpWorkerComponent =>

  val authInfo: AuthInfo
  implicit val jsonFormats: DefaultFormats.type = DefaultFormats

  trait AuthInfo {
    val baseUri: URI

    def getOutInfo(bagId: UUID, path: Path): Try[FileItemAuthInfo] = {
      for {
        f <- Try(URLEncoder.encode(path.toString, "UTF8"))
        uri = baseUri.resolve(s"$bagId/$f")
        jsonString <- http.getHttpAsString(uri)
      } yield parse(jsonString).extract[FileItemAuthInfo]
    }
  }
}
