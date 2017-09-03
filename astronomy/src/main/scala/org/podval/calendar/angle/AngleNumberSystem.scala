package org.podval.calendar.angle

import org.podval.calendar.numbers.NumberSystem.RawNumber
import org.podval.calendar.numbers.{NumberSystemMember, PointCompanion, RangedHeadDigitNumberSystem}


trait AngleNumberSystem extends RangedHeadDigitNumberSystem[AngleNumberSystem] {
  trait AngleNumberSystemMember extends NumberSystemMember[AngleNumberSystem] {
    final override def numberSystem: AngleNumberSystem = AngleNumberSystem.this
  }

  final override type Interval = AngleIntervalBase

  final type Angle = Interval

  final override def createInterval(raw: RawNumber): Interval =
    new Angle(raw) with AngleNumberSystemMember

  final override object Interval extends AngleCompanion with AngleNumberSystemMember

  final val Angle = Interval

  final override type Point = AnglePointBase

  final type AnglePoint = Point

  final override def createPoint(raw: RawNumber): Point =
    new AnglePoint(raw) with AngleNumberSystemMember

  final override object Point extends PointCompanion[AngleNumberSystem]
    with AngleNumberCompanion[Point] with AngleNumberSystemMember

  final val AnglePoint = Point

  final override def headRange: Int = 360

  final override def range(position: Int): Int = 60

  final override def headSign: String = "°"

  final override val signPartial: PartialFunction[Int, String] = {
    case 0 => "′"
    case 1 => "″"
    case 2 => "‴"
  }

  final override val defaultLength: Int = 3
}


object AngleNumberSystem extends AngleNumberSystem {
  import scala.language.implicitConversions

  implicit def angleToRadians(angle: Angle): Double = angle.toRadians
}
