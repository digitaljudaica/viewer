package org.opentorah.store

import org.opentorah.entity.{Entity, EntityReference}
import org.opentorah.metadata.{Name, Names}

final class EntityHolder(
  inheritedSelectors: Seq[Selector],
  urls: Urls,
  val entity: Entity
) extends Store(inheritedSelectors, urls) {

  override def names: Names = new Names(Seq(Name(entity.name)))

  override def references: Seq[EntityReference] = entity.references
}