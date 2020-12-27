package org.opentorah.metadata

import org.opentorah.xml.{Antiparser, Element, From, FromXml, Parser, Result}

final class Metadata[M](
  elementName: String,
  fromXml: FromXml[M]
) extends Element[Seq[M]](elementName) {
  override def parser: Parser[Seq[M]] = fromXml.all
  override def antiparser: Antiparser[Seq[M]] = ???
}

object Metadata {

  def load[K <: WithName, M](
    from: From,
    fromXml: FromXml[M],
    keys: Seq[K],
    hasName: (M, String) => Boolean
  ): Map[K, M] = Parser.parseDo(for {
    metadatas <- load[M](from, fromXml)

    result <- bind(
      keys,
      getName = (key: WithName) => key.name,
      metadatas,
      hasName
    )
  } yield result.toMap)

  def loadResource[M](
    obj: AnyRef,
    fromXml: FromXml[M]
  ): Seq[M] = Parser.parseDo(Metadata.load(
    from = From.resource(obj),
    fromXml
  ))

  def load[M](
    from: From,
    fromXml: FromXml[M]
  ): Parser[Seq[M]] = new Metadata[M](
    elementName = from.name,
    fromXml = fromXml
  ).parse(from)

  private def bind[K, M](
    keys: Seq[K],
    getName: K => String,
    metadatas: Seq[M],
    hasName: (M, String) => Boolean
  ): Parser[Seq[(K, M)]] = for {
    result <- Parser.collectAll(metadatas.map(metadata => find(keys, getName, metadata, hasName).map(_ -> metadata)))
    _ <- checkNoUnmatchedKeys(keys.toSet -- result.map(_._1).toSet)
  } yield result

  def bind[K, M](
    keys: Seq[K],
    metadatas: Seq[M],
    getKey: M => K
  ): Parser[Map[K, M]] = {
    val result = metadatas.map(metadata => getKey(metadata) -> metadata).toMap
    for {
      _ <- checkNoUnmatchedKeys(keys.toSet -- result.keySet)
    } yield result
  }

  private def checkNoUnmatchedKeys[K](unmatchedKeys: Set[K]): Result =
    Parser.check(unmatchedKeys.isEmpty, s"Unmatched keys: $unmatchedKeys")

  def find[K <: WithName](
    keys: Seq[K],
    names: Names
  ): Parser[K] = find(
    keys = keys,
    getName = (key: K) => key.name,
    metadata = names,
    hasName = (names: Names, name: String) => names.hasName(name)
  )

  private def find[K, M](
    keys: Seq[K],
    getName: K => String,
    metadata: M,
    hasName: (M, String) => Boolean
  ): Parser[K] = {
    val result: Seq[K] = keys.filter(key => hasName(metadata, getName(key)))
    for {
      _ <- Parser.check(result.nonEmpty, s"Unmatched metadata: $metadata")
      _ <- Parser.check(result.length == 1, s"Metadata matched multiple keys: $metadata")
    } yield result.head
  }
}
