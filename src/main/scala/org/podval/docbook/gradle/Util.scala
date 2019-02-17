package org.podval.docbook.gradle

import java.io.{BufferedWriter, File, FileWriter, InputStream}

object Util {
  def fileNameWithoutExtension(file: File): String = {
    val result: String = file.getName
    val lastDot: Int = result.lastIndexOf(".")
    result.substring(0, lastDot)
  }

  def drop(what: Seq[String], from: String): Option[String] =
    what.flatMap(drop(_, from)).headOption

  def drop(what: String, from: String): Option[String] =
    if (from.startsWith(what)) Some(from.drop(what.length)) else None

  def subdirectory(directory: File, subdirectoryName: Option[String]): File =
    subdirectoryName.fold(directory)(new File(directory, _))

  def deleteRecursively(file: File): Unit = {
    if (file.isDirectory)
      file.listFiles.foreach(deleteRecursively)
    if (file.exists && !file.delete)
      throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
  }

  def readFrom(file: File): String =
    scala.io.Source.fromFile(file).getLines.mkString("\n")

  def readFrom(is: InputStream): String =
    scala.io.Source.fromInputStream(is).getLines.mkString("\n")

  def writeInto(file: File, what: String): Unit = {
    file.getParentFile.mkdirs()
    val writer: BufferedWriter = new BufferedWriter(new FileWriter(file))
    try { writer.write(what.stripMargin) }
    finally { writer.close() }
  }

  //  def fileInputSource(file: File): InputSource =
  //    new InputSource(file.toURI.toASCIIString)

  //  def stringInputSource(input: String, publicId: Option[String], systemId: Option[String]): InputSource = {
  //    val result = new InputSource(new StringReader(input))
  //    result.setPublicId(publicId.orNull)
  //    result.setSystemId(systemId.orNull)
  //    result
  //  }

  //  def resourceInputSource(name: String): InputSource = {
  //    val result = new InputSource(getClass.getResourceAsStream(name))
  //    result.setSystemId(name)
  //    result.setPublicId(null)
  //    result
  //  }

  //  def fileSource(file: File): Source =
  //    new StreamSource(file)

  //  def stringSource(input: String, publicId: Option[String], systemId: Option[String]): Source = {
  //    val result = new StreamSource(new StringReader(input))
  //    result.setPublicId(publicId.orNull)
  //    result.setSystemId(systemId.orNull)
  //    result
  //  }
}
