# Cats Effect Continuous Passing Style Transform
A prototype library to support `async` / `await` style in Scala 3 using an advanced macro system!

## Why?

Idiomatically, in Scala is already possible to handle concurrency using `monadic` manipulation
(e.g., Future, IO, ...).
Unfortunately, this introduces an accidental
complexity inside the program, making the program harder to read and
more complicated to maintain.
Consider, for instance, the following piece of code:
```scala
for
  userData <- fetchFromServer("...")
manipulation = manipulate(userData)
_ <- ifM(isOk(manipulation)) {
  IO.println("Ok!")
} {
  IO.println("Ko, problem")
}
yield ()
```
Even if for the one that knows scala could be pretty clear, several things are distracting:
- `for` and `yield` keywords are used only to enable the monadic syntax, instead of using `map` and `flatMap`
- You have to rember the difference between `=` and `<-`
- The output of pure side effect (in this case, `println`) are not considered, therefore the for-comprehension is typically filled of `_ <- `

Partially, these problems could be handled using an ad-hoc dsl (e.g.,`>>` `*>` `<*` of cats effect), but still this introduces another level of complexity.
Other languages, like JS, support concurrent flows using `async` / `await`.
This is easier to understand and feels like writing an imperative program, even if it is asynchronous!
We would like to write something like this:

```scala
async[IO] { // IO for devise the "context" in which side effect happen
  val userData = await(fetchFromServer)
  val manipulation = manipulate(userData)
  if(isOk(manipulation)) IO.println("Ok!").await else IO.println("Ko, problem")
}
```
It is possible then to use the second way of expressing concurrent programming and rewriting it
under-the-hood as a monadic manipulation?
This is the question that let the developer of [`dotty-cps-async`](https://github.com/rssh/dotty-cps-async) start to work in a rewriting system that converts `await` `async` style in
monadic manipulation. In fact, several others try to create a rewriting system, just to mention some of scala 2:
- [Monadless](https://github.com/monadless/monadless): leverage scala 2 macro system to create conversion
- [Effectful](https://github.com/pelotom/effectful): similar to monadless, but more cryptic
- [Async](https://github.com/scala/scala-async): runtime reflection to convert async and await code. It has several limitations

Unfortunately, all of them are considered unstable,
therefore in `dotty-cps-async` the developers try to create a general and robust framework in scala 3,
in which they use the macro system of scala 3 to perform a compile-time rewriting of `async` `await` operators
using CPS passing transform.
[Here](https://www.slideshare.net/rssh1/embedding-generic-monadic-transformer-into-scala-tfp2022) more details about how they implement this conversion.
In this repository there are some examples of how this library could be used for one of the most famous functions asynchronous runtime systems in Scala,
that is [Cats effect](https://typelevel.org/cats-effect/).
Particularly:
- in [it.unibo.cats.example.basic]() there are several examples written both in the standard monadic way and with `async` and `await` syntax
- in [it.unibo.cats.example.game]() there is a simple console application developed with `await` and `async` syntax
