package it.unibo.cats

import cats.effect.IO
import cps.CpsAwaitable
import cats.effect.cps.*

object dsl:
  extension [T](io: IO[T])
    transparent inline def unary_!(using CpsAwaitable[IO]) :T = io.await

    def logThread(): IO[T] = async[IO] {
      IO.println(s"thread = ${Thread.currentThread()}").await
      io.await
    }


  def unsafeRun[T](io: IO[T])(using cats.effect.unsafe.IORuntime): T =
    io.unsafeRunSync()