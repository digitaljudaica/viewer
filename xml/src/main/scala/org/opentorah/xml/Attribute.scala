package org.opentorah.xml

import org.w3c.dom.{Document, Element => DomElement}
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl
import zio.ZIO

// Type-safe XML attribute get/set - for use in DOM and SAX inspired by
//   net.sourceforge.jeuclid.context.Parameter and friends.
abstract class Attribute[T](val name: String) extends Conversion[T] with Requireable[T] {

  final override def toString: String = s"attribute $name"

  def namespace: Option[Namespace] = None

  def default: T

  // Scala XML

  final override def optional: Parser[Option[T]] = Context.takeAttribute(name).flatMap { value =>
    value.fold[Parser[Option[T]]](ZIO.none)(parseFromString(_).map(Some(_)))
  }

  final def withValue(value: Option[T]): Attribute.Value[T] = new Attribute.Value[T](this, value)

  final def withValue(value: T): Attribute.Value[T] = withValue(Some(value))

  // DOM

  final def get(document: Document): Option[T] = {
    val element: DomElement = document.getDocumentElement
    val inDefaultNamespace: Boolean = namespace.isEmpty
    get(if (inDefaultNamespace) element.getAttribute(name) else element.getAttributeNS(namespace.get.uri, name))
  }

  final def doGet(document: Document): T = get(document).get

  final def getWithDefault(document: Document): T = get(document).getOrElse(default)

  final def set(value: T, document: Document): Unit = {
    val element = document.getDocumentElement
    val inDefaultNamespace: Boolean = namespace.isEmpty || namespace.get.is(element.getNamespaceURI)
    if (inDefaultNamespace) element.setAttribute(name, toString(value))
    else {
      // declare the attribute's namespace if it is not declared
      namespace.get.ensureDeclared(element)

      element.setAttributeNS(namespace.get.uri, namespace.get.qName(name), toString(value))
    }
  }

  // SAX

  final def get(defaultNamespace: Namespace, attributes: Attributes): Option[T] = {
    // Note: when attribute is in the default namespace, it needs to be retrieved accordingly.
    // This is needed for the 'display' attribute (the only attribute this method is used for) of the included MathML -
    // but somehow does not seem to break the inline MathML either :)
    val inDefaultNamespace: Boolean = namespace.isEmpty || namespace.get.is(defaultNamespace)

    get(attributes.getValue(
      if (inDefaultNamespace) "" else namespace.get.uri,
      name
    ))
  }

  final def set(defaultNamespace: Namespace, value: T, attributes: AttributesImpl): Unit = {
    // Note: when attribute is added with the default namespace, this is not detected and a new namespace
    // with the same URI gets auto-declared - is this a bug or a feature?
    // Work-around: set the attribute *without* the namespace when I know that it is the default one!
    val inDefaultNamespace: Boolean = namespace.isEmpty || namespace.get.is(defaultNamespace)

    /* Note: if namespace.ensureDeclared() *is* called, I get error parsing the resulting fo:
      org.xml.sax.SAXParseException
      The prefix "xmlns" cannot be bound to any namespace explicitly; neither can the namespace for "xmlns" be bound to any prefix explicitly.
          at org.apache.xerces.parsers.AbstractSAXParser.parse(Unknown Source)
          at org.apache.xerces.jaxp.SAXParserImpl$JAXPSAXParser.parse(Unknown Source)
          at com.icl.saxon.IdentityTransformer.transform(IdentityTransformer.java:59)
      If it is not - there is still somehow "mathjax" namespace declaration in the output of the MathReader...

      if (!inDefaultNamespace) namespace.ensureDeclared(attributes)
     */

    attributes.addAttribute(
      if (inDefaultNamespace) "" else namespace.get.uri,
      name,
      if (inDefaultNamespace) name else namespace.get.qName(name),
      "CDATA",
      toString(value)
    )
  }

  def setWithDefault(defaultNamespace: Namespace, value: Option[T], attributes: AttributesImpl): Unit =
    set(defaultNamespace, value.getOrElse(default), attributes)

  // helpers

  private def get(value: String): Option[T] =
    Option(value).filter(_.nonEmpty).map(fromString)

}

object Attribute {

  final class Value[A](
    val attribute: Attribute[A],
    value: Option[A]
  ) {
    def valueToString: Option[String] = value.map(attribute.toString)
  }

  def apply(name: String): Attribute[String] = new Attribute[String](name) with Conversion.StringConversion {
    override def default: String = ""
  }

  final class BooleanAttribute(name: String) extends Attribute[Boolean](name) with Conversion.BooleanConversion {
    override def default: Boolean = false
    def orFalse: Parser[Boolean] = optional.map(_.getOrElse(false))
  }

  object BooleanAttribute {
    def apply(name: String): BooleanAttribute = new BooleanAttribute(name)
  }

  final class IntAttribute(name: String) extends Attribute[Int](name) with Conversion.IntConversion {
    override def default: Int = 0
  }

  object IntAttribute {
    def apply(name: String): IntAttribute = new IntAttribute(name)
  }

  final class PositiveIntAttribute(name: String) extends Attribute[Int](name) with Conversion.PositiveIntConversion {
    override def default: Int = 1
  }

  object PositiveIntAttribute {
    def apply(name: String): PositiveIntAttribute = new PositiveIntAttribute(name)
  }

  abstract class FloatAttribute(name: String) extends Attribute[Float](name) with Conversion.FloatConversion

  val id: Attribute[String] = Attribute("xml:id")

  val allAttributes: Parser[Map[String, String]] =
    Context.takeAllAttributes
}
