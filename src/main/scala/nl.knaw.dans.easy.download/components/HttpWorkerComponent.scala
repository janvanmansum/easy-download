package nl.knaw.dans.easy.download.components

import java.net.URI

import nl.knaw.dans.easy.download.{ HttpStatusException, OutputStreamProvider }
import org.apache.commons.io.IOUtils
import org.eclipse.jetty.http.HttpStatus.OK_200

import scala.util.{ Failure, Success, Try }
import scalaj.http.{ Http, HttpResponse }

trait HttpWorkerComponent {

  val http: HttpWorker

  trait HttpWorker {
    def copyHttpStream(uri: URI): OutputStreamProvider => Try[Unit] = { outputStreamProducer =>
      val response = Http(uri.toString).method("GET").exec {
        case (OK_200, _, is) => IOUtils.copyLarge(is, outputStreamProducer())
        case _ => // do nothing
      }
      if (response.isSuccess) Success(())
      else failed(uri, response)
    }

    def getHttpAsString(uri: URI): Try[String] = {
      val response = Http(uri.toString).method("GET").asString
      if (response.isSuccess) Success(response.body)
      else failed(uri, response)
    }

    private def failed(uri: URI, response: HttpResponse[_]) = {
      Failure(HttpStatusException(s"Could not download $uri", HttpResponse(response.statusLine, response.code, response.headers)))
    }
  }
}
