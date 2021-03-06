package org.opentorah.texts.tanach

import org.opentorah.metadata.{Names, WithNumber}
import org.opentorah.xml.{ContentType, Element, Parsable, Parser, Unparser}
import zio.ZIO

case object Psalms extends Writings {

  final class BookMetadata(
    val days: Seq[Span],
    val weekDays: Seq[Span],
    val books: Seq[Span]
  ) extends Nach.BookMetadata(Psalms)

  final class Parsed(
    names: Names,
    chapters: Chapters,
    val days: Seq[Span],
    val weekDays: Seq[Span],
    val books: Seq[Span]
  ) extends Nach.Parsed(Psalms, names, chapters) {

    override def resolve: Parser[BookMetadata] = ZIO.succeed(new BookMetadata(
      days,
      weekDays,
      books
    ))
  }

  override def parser(names: Names, chapters: Chapters): Parser[Parsed] = for {
    days <- spansParser(chapters, "day", 30)
    weekDays <- spansParser(chapters, "weekDay", 7)
    books <- spansParser(chapters, "book", 5)
  } yield new Parsed(names, chapters, days, weekDays, books)

  private def spansParser(chapters: Chapters, name: String, number: Int): Parser[Seq[Span]] = for {
    numbered <- new SpanParsable(name).seq()
    _ <- WithNumber.checkNumber(numbered, number, name)
  } yield SpanSemiResolved.setImpliedTo(WithNumber.dropNumbers(numbered).map(_.semiResolve), chapters.full, chapters)

  private final class SpanParsable(name: String) extends Element[WithNumber[SpanParsed]](name) {
    override def contentType: ContentType = ContentType.Empty

    override def contentParsable: Parsable[WithNumber[SpanParsed]] = new Parsable[WithNumber[SpanParsed]] {
      override def parser: Parser[WithNumber[SpanParsed]] = WithNumber.parse(SpanParsed.parser)
      override def unparser: Unparser[WithNumber[SpanParsed]] = ???
    }
  }

  def days: Seq[Span] = metadata.days

  def weekDays: Seq[Span] = metadata.weekDays

  def books: Seq[Span] = metadata.books

  override def metadata: BookMetadata = Tanach.forBook(Psalms).asInstanceOf[BookMetadata]
}
