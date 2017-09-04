package org.podval.calendar.gregorian

import org.podval.calendar.dates.MomentBase
import Gregorian.Moment

abstract class GregorianMoment(digits: Seq[Int]) extends MomentBase[Gregorian](digits) {
  final def morningHours(value: Int): Moment = firstHalfHours(value)

  final def afternoonHours(value: Int): Moment = secondHalfHours(value)
}
