package org.opentorah.dates

import org.opentorah.metadata.NamedCompanion
import org.opentorah.numbers.NumbersMember

/**
  *
  */
trait DayCompanion[C <: Calendar[C]] extends NumbersMember[C] {
  val Name: NamedCompanion

  final type Name = Name.Key

  def names: Seq[C#DayName]

  final def apply(number: Int): C#Day = apply(None, number)

  private[opentorah] final def witNumberInMonth(month: C#Month, numberInMonth: Int): C#Day = {
    require (0 < numberInMonth && numberInMonth <= month.length)
    apply(Some(month), month.firstDayNumber + numberInMonth - 1)
  }

  private[opentorah] def apply(monthOpt: Option[C#Month], number: Int): C#Day

  final def numberInWeek(dayNumber: Int): Int =
    ((dayNumber + firstDayNumberInWeek - 1 - 1) % Calendar.daysPerWeek) + 1

  val firstDayNumberInWeek: Int
}
