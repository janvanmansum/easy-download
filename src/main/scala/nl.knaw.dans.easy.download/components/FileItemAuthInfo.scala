package nl.knaw.dans.easy.download.components

import java.io.FileNotFoundException

import nl.knaw.dans.easy.download.NotAllowedException
import org.joda.time.DateTime

import scala.util.{ Failure, Success, Try }

case class FileItemAuthInfo(itemId: String, owner: String, dateAvailable: String, accessibleTo: String, visibleTo: String) {
  private val dateAvailableMilis: Long = new DateTime(dateAvailable).getMillis

  def canSee(user: Option[User]): Try[Unit] = {
    (user, visibleTo) match {
      case (None, "ANONYMOUS") => Success(())
      case (None, _) => Failure(new FileNotFoundException(itemId))
      case (Some(_), _) => Failure(new NotImplementedError())
    }
  }

  def canDownload(user: Option[User]): Try[Unit] = {
    (user, accessibleTo) match {
      case (None, "ANONYMOUS") => Success(())
      case (None, _) => Failure(NotAllowedException(s"download not allowed of: $itemId"))
      case (Some(_), _) => Failure(new NotImplementedError())
    }
  }

  def noEmbargo: Try[Unit] = {
    if (dateAvailableMilis <= DateTime.now.getMillis) Success(())
    else Failure(NotAllowedException(s"download becomes available on $dateAvailableMilis [$itemId]"))
  }
}
