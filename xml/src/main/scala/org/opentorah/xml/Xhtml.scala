package org.opentorah.xml

object Xhtml extends Dialect {

  override val namespace: Namespace = Namespace(uri="http://www.w3.org/1999/xhtml", prefix="xhtml")

  override val mimeType: String = "application/xhtml+xml"
}