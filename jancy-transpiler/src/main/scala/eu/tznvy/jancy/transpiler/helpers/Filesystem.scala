package eu.tznvy.jancy.transpiler.helpers

import java.io.InputStream
import java.nio.file.Path

/**
  * A wrapper for IO operations
  */
trait Filesystem {
  def createDirectories(path: Path): Unit

  def writeFile(path: Path, content: String): Unit

  def readFile(path: Path): Option[String]

  def testPath(path: Path): Boolean

  def copy(from: InputStream, to: Path): Unit
}
