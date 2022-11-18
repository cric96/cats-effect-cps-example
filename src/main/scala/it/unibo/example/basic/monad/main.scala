package it.unibo.example.basic.monad
import it.unibo.cats.dsl.*
import cats.effect.IO
import cats.effect.ParallelF
import cats.effect.unsafe.implicits.global
import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
/**
 * Set of simple examples that show how to express side effect using cats effect
 * goals:
 * a) show how to compose IO => for compression and ad-hoc syntex
 * b) show some simple computation definitions
 * c) highlight the problems => not easy to read
 */

@main def printlnExample = unsafeRun {
  IO.println("Hello")
}

@main def inOutExample = unsafeRun {
  /*
   +1 express of side effects as data structure, decoupling the runtime/execution that will be used
   +1 eager/lazy computation, everything will be executed only at "the end of the world"
   +1 using for-comprehension, it "feels" like expressing an imperative code block
   -1 even if it is easy, it hides the intent, namely expressing a program that print a string, waits an input and the print that one
      -> for and yield keyword confuse the programmers
   */
  for
    _ <- IO.println("Hello! How do u feel today?")
    line <- IO.readLine
    _ <- IO.println(line)
  yield ()
}

@main def loop1 = unsafeRun {
  // With "IO" monad, u can express any kind of computation, e.g., infinite loops!
  def loop: IO[Unit] = for
    _ <- IO.print("Na")
    _ <- IO.sleep(1 seconds)
    _ <- loop
  yield ()
  loop
}

@main def loop2 = unsafeRun {
  /*
   shortcuts when the computation perform only side effects, without using the result of each IO.
   */
  IO.print("Na") >> IO.sleep(1 seconds) foreverM
}

@main def choice = unsafeRun {
  /*
  Obviously u can express if condition
  */
  for
    head <- IO(Random.nextBoolean())
    _ <- if(head) IO.println("Head!") else IO.println("Cross!")
  yield()
}

@main def nesting = unsafeRun {
  /**
   * You can nested several for comprehension, but this will make the program harder to read!
   */
  for
    _ <- IO.println("Welcome!")
    _ <- for
      _ <- IO.println("Nesting examples")
      time <- IO.realTime
      _ <- IO.println(s"Everything work as expected! ${time}")
    yield ()
  yield()
}

@main def parallelComputation = unsafeRun {
  /* By default, IO are concurrent but NOT parallel, to make it parallel, you should use par* methods */
  val computation = IO.println("Parallel!!").logThread()
  IO.parSequenceN(Runtime.getRuntime.availableProcessors())(List.fill(10)(computation))
}

@main def catsEffectExample = unsafeRun {
  // From https://typelevel.org/cats-effect/docs/getting-started
  // Main problem here => if you don't know the dsl and some advanced aspects, this code isn't clear at all!
  // what is the meaning of *>? why sometimes it use = and other time <-?
  for
    counter <- IO.ref(0) // concurrent reference, it is used in this case since it is shared with several fiber
    wait = IO.sleep(1.second)
    poll = wait *> counter.get // perform wait and get the value of the right side IO

    _ <- poll.flatMap(IO.println(_)).foreverM.start // detach the computation from them main thread, print each number (i.e., it create a fiber)
    _ <- poll.map(_ % 3 == 0).ifM(IO.println("fizz"), IO.unit).foreverM.start
    _ <- poll.map(_ % 5 == 0).ifM(IO.println("buzz"), IO.unit).foreverM.start
    _ <- (wait *> counter.update(_ + 1)).foreverM.void
  yield ()

}