package nl.knaw.dans.easy.download.components

import java.io.FileNotFoundException

import nl.knaw.dans.easy.download.NotAllowedException
import nl.knaw.dans.easy.download.components.RightsFor._
import org.joda.time.DateTime

import scala.util.{ Failure, Success, Try }

case class FileItemAuthInfo(itemId: String,
                            owner: String,
                            dateAvailable: String,
                            accessibleTo: String,
                            visibleTo: String
                           ) {
  private val dateAvailableMilis: Long = new DateTime(dateAvailable).getMillis

  // TODO apply json type hints in AuthInfoComponent to change type of arguments
  private val visibleToValue = RightsFor.withName(visibleTo)
  private val accessibleToValue = RightsFor.withName(accessibleTo)

  def canSee(user: Option[User]): Try[Unit] = {
    (user, visibleToValue) match {
      case (None, ANONYMOUS) => Success(())
      case (None, _) => Failure(new FileNotFoundException(itemId))
      case (Some(_), _) => Failure(new NotImplementedError())
    }
  }

  def canDownload(user: Option[User]): Try[Unit] = {
    (user, accessibleToValue) match {
      case (None, ANONYMOUS) => Success(())
      case (None, _) => Failure(NotAllowedException(s"download not allowed of: $itemId"))
      case (Some(_), _) => Failure(new NotImplementedError())
    }
  }

  def noEmbargo: Try[Unit] = {
    if (dateAvailableMilis <= DateTime.now.getMillis) Success(())
    else Failure(NotAllowedException(s"download becomes available on $dateAvailableMilis [$itemId]"))
  }
}
