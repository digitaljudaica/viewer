package org.opentorah.tei

import org.opentorah.xml.{Antiparser, Choice, Element, Parser}

final case class ProfileDesc(
  documentAbstract: Option[Abstract.Value],
  creation: Option[Creation],
  langUsage: Option[LangUsage],
  textClass: Option[TextClass.Value],
  correspDesc: Option[CorrespDesc.Value],
  calendarDesc: Option[CalendarDesc.Value],
  handNotes: Option[HandNotes],
  listTranspose: Option[ListTranspose.Value]
)

object ProfileDesc extends Element[ProfileDesc]("profileDesc") {

  override val parser: Parser[ProfileDesc] = for {
    values <- Choice(Seq(
      Abstract.parsable,
      Creation,
      LangUsage,
      TextClass.parsable,
      CorrespDesc.parsable,
      CalendarDesc.parsable,
      HandNotes,
      ListTranspose.parsable
    ))
    documentAbstract <- values.optional(Abstract.parsable)
    creation <- values.optional(Creation)
    langUsage <- values.optional(LangUsage)
    textClass <- values.optional(TextClass.parsable)
    correspDesc <- values.optional(CorrespDesc.parsable)
    calendarDesc <- values.optional(CalendarDesc.parsable)
    handNotes <- values.optional(HandNotes)
    listTranspose <- values.optional(ListTranspose.parsable)
  } yield new ProfileDesc(
    documentAbstract,
    creation,
    langUsage,
    textClass,
    correspDesc,
    calendarDesc,
    handNotes,
    listTranspose
  )

  override val antiparser: Antiparser[ProfileDesc] = Tei.concat(
    Abstract.parsable.toXmlOption(_.documentAbstract),
    Creation.toXmlOption(_.creation),
    LangUsage.toXmlOption(_.langUsage),
    TextClass.parsable.toXmlOption(_.textClass),
    CorrespDesc.parsable.toXmlOption(_.correspDesc),
    CalendarDesc.parsable.toXmlOption(_.calendarDesc),
    HandNotes.toXmlOption(_.handNotes),
    ListTranspose.parsable.toXmlOption(_.listTranspose)
  )

  def apply(): ProfileDesc = new ProfileDesc(
    documentAbstract = None,
    creation = None,
    langUsage = None,
    textClass = None,
    correspDesc = None,
    calendarDesc = None,
    handNotes = None,
    listTranspose = None
  )
}
