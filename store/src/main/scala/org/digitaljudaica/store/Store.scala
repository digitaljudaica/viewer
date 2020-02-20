package org.digitaljudaica.store

import cats.implicits._
import java.io.File
import org.digitaljudaica.metadata.Names
import org.digitaljudaica.xml.{ContentType, From, Parser, Xml}
import scala.xml.Elem

sealed trait Store {
  def names: Names
  def by: Option[By]
}

final class BaseStore(
  override val names: Names,
  selectors: Seq[Selector],
  baseBy: By
) extends Store {
  override def by: Option[By] = Some(baseBy)
}

final class TextStore(
  name: String,
  url: String
) extends Store {
  private var textCache: Option[Elem] = None

  def text: Elem = {
    if (textCache.isEmpty) textCache = Some(From.file(new File(url), s"$name.xml").loadDo)
    textCache.get
  }

  override def names: Names = ??? // TODO based on name
  override def by: Option[By] = None // TODO internal structure of the text (pages, chapters...)
}

object Store {

  def parser(inheritedSelectors: Set[Selector]): Parser[Store] = Xml.withName("store", for {
    names <- Names.withDefaultNameParser
    selectors <- Xml.element.all("selector",ContentType.Elements,  Selector.parser)
    _ = println(names)
    _ = println(selectors)
    by <- Xml.element.required("by", ContentType.Elements, byParser(inheritedSelectors ++ selectors.toSet))
  } yield new BaseStore(
    names,
    selectors,
    by
  ))

  val textsParser: Parser[String] = for {
    url <- Xml.attribute.required("url")
  } yield url

  // TODO allow 'by' to be named in-line
  def byParser(selectors: Set[Selector]): Parser[By] = for {
    n <- Xml.attribute.required("n")
    selector = selectorByName(selectors, n)
    texts <- Xml.element.optional("texts", ContentType.Elements, textsParser)
    stores <- Xml.element.all("store", ContentType.Elements, parser(selectors))
    _ <- Parser.check(texts.nonEmpty || stores.nonEmpty, "Both 'stores' and 'texts' elements are absent.")
    _ <- Parser.check(texts.isEmpty || stores.isEmpty, "Both 'stores' and 'texts' elements are present.")
  } yield {
    if (selector.isEmpty) throw new IllegalArgumentException(s"Selector not found: $n")
    if (texts.isDefined) new TextsBy(selector.get, texts.get) else new BaseBy(selector.get, stores)
  }

  def selectorByName(selectors: Set[Selector], name: String): Option[Selector] = {
    val result = selectors.find(_.names.hasName(name))
    result
  }
}
