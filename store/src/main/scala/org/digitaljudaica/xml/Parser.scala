package org.digitaljudaica.xml

import cats.data.StateT
import cats.implicits._

object Parser {

  def pure[A](value: A): Parser[A] = StateT.pure[ErrorOr, Context, A](value)

  def lift[A](value: ErrorOr[A]): Parser[A] = StateT.liftF[ErrorOr, Context, A](value)

  def error[A](value: Error): Parser[A] = lift(Left(value))

  private[xml] def inspect[A](f: Context => A): Parser[A] = StateT.inspect[ErrorOr, Context, A](f)

  private[xml] def set(context: Context): Parser[Unit] = StateT.set[ErrorOr, Context](context)

  private[xml] def modify(f: Context => Context): Parser[Unit] = StateT.modify[ErrorOr, Context](f)

  def check(condition: Boolean, message: => String): Parser[Unit] =
    if (condition) pure(()) else error[Unit](message)

  private[xml] def required[A](what: String, parser: Parser[Option[A]]): Parser[A] = for {
    result <- parser
    _ <- check(result.isDefined, s"Required $what is missing")
  } yield result.get

  def toErrorOr[A](f: => A): ErrorOr[A] =
    try { Right(f) } catch { case e: Exception => Left(e.getMessage) }

  def toParser[A](f: => A): Parser[A] = lift(toErrorOr(f))
}
