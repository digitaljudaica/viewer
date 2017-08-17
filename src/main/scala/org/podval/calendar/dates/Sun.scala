package org.podval.calendar.dates

// TODO Where and when was the Sun created? Does this jibe with Rambam's epoch?
// TODO Which day of the week (+1/-1) was the Giving of the Law? (Sema)
// TODO Rambam's epoch - two days after molad?! (Petya Ofman)
// TODO angular speed of the moon = 360 / (1/tropical month + 1/solar year)
object Sun {
  import org.podval.calendar.jewish.Jewish.{interval, Year, MonthName, TimeInterval}

  val yearOfShmuel = interval.days(365).hours(6)


  val yearOfRavAda: TimeInterval = Year.cycleLength / Year.yearsInCycle


  // KH 9:3

  val firstTkufasNissan = Year(1).month(MonthName.Nisan).newMoon - interval.days(7).hours(9).parts(642)  // KH 9:3


  // Sun enters Teleh  KH 9:3
  //  def tkufasNissan(year: Int) = firstTkufasNissan + yearOfRavAda * (year-1)
  def tkufasNissan(year: Int) = firstTkufasNissan + Sun.yearOfShmuel * (year-1)


  // Tkufas Tammuz - Sartan; Tishrei - Moznaim; Teves - Gdi.  KH 9:3


  // Since Birkas HaChama is said in the morning, we add 12 hours to the time of the equinox
  // Sanctification of the Sun falls from Adar 10 to Nissan 26.
  // Only 27 days in Adar and Nissan have have the sanctification of the Sun happen on them at least once.
  // It never happens on Passover.
  // It happens more often than on the Passover Eve on 7 days.
  def birkasHachama(cycle: Int) = firstTkufasNissan + yearOfShmuel * 28 * cycle + interval.hours(12)
}
