package org.opentorah.tei

import org.opentorah.xml.{Antiparser, Element, Parser}
import scala.xml.Node

final case class TitleStmt(
  titles: Seq[Title.Value],
  authors: Seq[Author.Value],
  editors: Seq[Editor],
  sponsors: Seq[Sponsor.Value],
  funders: Seq[Funder.Value],
  principals: Seq[Principal.Value],
  respStmts: Seq[RespStmt.Value]
) {
  def references: Seq[EntityReference] = {
    val xml: Seq[Node] =
      Title.parsable.elementAntiparserSeq.content(titles) ++
      Author.parsable.elementAntiparserSeq.content(authors) ++
      Sponsor.parsable.elementAntiparserSeq.content(sponsors) ++
      Funder.parsable.elementAntiparserSeq.content(funders) ++
      Principal.parsable.elementAntiparserSeq.content(principals) ++
      RespStmt.parsable.elementAntiparserSeq.content(respStmts)

    EntityReference.from(xml) ++ editors.flatMap(_.persName.toSeq)
  }
}

object TitleStmt extends Element.WithToXml[TitleStmt]("titleStmt") {
  def apply(): TitleStmt = new TitleStmt(
    titles = Seq.empty,
    authors = Seq.empty,
    editors = Seq.empty,
    sponsors = Seq.empty,
    funders = Seq.empty,
    principals = Seq.empty,
    respStmts = Seq.empty
  )

  override protected val parser: Parser[TitleStmt] = for {
    titles <- Title.parsable.all
    authors <- Author.parsable.all
    editors <- Editor.all
    sponsors <- Sponsor.parsable.all
    funders <- Funder.parsable.all
    principals <- Principal.parsable.all
    respStmts <- RespStmt.parsable.all
  } yield new TitleStmt(
    titles,
    authors,
    editors,
    sponsors,
    funders,
    principals,
    respStmts
  )

  override protected val antiparser: Antiparser[TitleStmt] = Antiparser(
    Title.parsable.elementAntiparserSeq.compose(_.titles),
    Author.parsable.elementAntiparserSeq.compose(_.authors),
    Editor.elementAntiparserSeq.compose(_.editors),
    Sponsor.parsable.elementAntiparserSeq.compose(_.sponsors),
    Funder.parsable.elementAntiparserSeq.compose(_.funders),
    Principal.parsable.elementAntiparserSeq.compose(_.principals),
    RespStmt.parsable.elementAntiparserSeq.compose(_.respStmts)
  )
}