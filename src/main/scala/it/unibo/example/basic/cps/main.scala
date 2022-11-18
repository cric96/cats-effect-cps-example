package it.unibo.example.basic.cps
import it.unibo.cats.dsl.*
import cats.effect.cps.*
import cats.effect.IO
import cats.effect.ParallelF
import cats.effect.unsafe.implicits.global
import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
/**
 * These example are easy, but it will be clear that the programs are easier to read.
 * The good point is that the performance continue to be the same -- the rewriting in the monadic form
 * is performed at compile time!
 */

@main def printlnExample = async[IO] { // simil
  IO.println("Hello").await // similar to js way => await promise
}.unsafeRunSync()

@main def inOutExample1 = async[IO] {
  /**
   * +1 maintain the same semantics of the monadic manipulation
   * +1 it's easy to read, as the other alternatives (js and rust)
   * -1 u need to remember to use .await, otherwise nothing will happen
   */
  IO.println("Hello! How do u feel today?").await
  val line = IO.readLine.await
  IO.println(line).await
}.unsafeRunSync()

@main def inOutExample2 = async[IO] {
  /**
   * it is possible to use ! instead of await at the end
   */
  !IO.println("Hello! How do u feel today?")
  val line = !IO.readLine
  !IO.println(line)
}.unsafeRunSync()

@main def loop1 =
  // You can easily use async in function definition, it will return a IO[X]
  // it is similar to Js async function () {...} that will return a Promise
  def loop: IO[Unit] = async[IO] {
    !IO.print("Na")
    !IO.sleep(1 seconds)
    !loop
  }
  loop.unsafeRunSync()


@main def loop2 = async[IO] {
  /*
  you can use the same dsl of standard IO manipulation
   */
  val program = IO.print("Na") >> IO.sleep(1 seconds) foreverM

  !program
}.unsafeRunSync()

@main def choice = async[IO] {
  val head = !IO(Random.nextBoolean())
  if(head) !IO.println("Head") else !IO.println("Cross!")
}.unsafeRunSync()

@main def nesting = async[IO] {
  // Yoy can nest async call, even if it probably has no sense at all => you will make a function call instead
  !IO.println("Welcome!")
  async[IO] {
    !IO.println("Nesting examples")
    val time = !IO.realTime
    !IO.println(s"Everything work as expected! ${time}")
  }.await
}.unsafeRunSync()

@main def parallelComputation = async[IO] {
  val computation = IO.println("Parallel!!").logThread().map(_ => "ciao")
  List.fill(10)(computation).map(io => io.start.await) // a way to express parallel computation is to express fiber starts..
}.unsafeRunSync()

@main def catsEffectExample = {
  async[IO] {
    val wait = IO.sleep(1 second)
    val counter = !IO.ref(0)
    val poll: IO[Int] = async[IO] {
      !wait
      !counter.get
    }
    // express concurrent computation (look! without ! or await)
    val printNumber = async[IO] {
      val counter = !poll
      !IO.println(counter)
    }
    val printFizz = async[IO] { if(!poll % 3 == 0) !IO.println("fizz") }
    val printBuzz = async[IO] { if(!poll % 5 == 0) !IO.println("buzz")
    }
    // effectively execute the computations
    printNumber :: printFizz :: printBuzz :: Nil foreach (_.foreverM.start.await) // parallel computation
    // main update
    def updateCounter: IO[Unit] = async[IO] {
      !wait
      !counter.update(_ + 1)
      !updateCounter
    }
    !updateCounter
  }.unsafeRunSync()
}
