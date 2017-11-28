package nl.knaw.dans.easy.download.components

import java.nio.file.{ Path, Paths }
import java.util.UUID

import org.joda.time.DateTime

case class FileItemAuthInfo (itemId: String, owner: String, available: String, accessibleTo: String, visibleTo: String) {
  val (uuid: UUID, path: Path) = {
    val all = Paths.get(itemId)
    val root = all.getRoot
    (UUID.fromString(root.toString), all.relativize(root))
  }
  val dateAvailable: DateTime = new DateTime(available)
}
