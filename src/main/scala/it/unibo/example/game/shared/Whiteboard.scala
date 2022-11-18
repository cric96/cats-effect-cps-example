package it.unibo.example.game.shared
import cats.effect
import cats.effect.IO
import cats.effect.cps.*
import it.unibo.cats.dsl.*
import cats.effect.kernel.Ref

object Whiteboard:

  def apply(): IO[Ref[IO, List[Todo]]] = async[IO] {
    !IO.ref(List.empty[Todo])
  }

  extension(ref: Ref[IO, List[Todo]])
    def todos: IO[List[Todo]] = async[IO] {
      !ref.get
    }

    def get(name: String): IO[Todo] = async[IO] {
      val todo = ref.todos.await.find(_.name == name)
      !IO.fromOption(todo)(IllegalArgumentException(s"No ${name} in the whiteboard"))
    }

    def insert(todo: Todo): IO[Unit] = async[IO] {
      !ref.update(todos => todo :: todos)
    }

    def remove(todo: String): IO[Unit] = async[IO] {
      !ref.update(todos => todos.filter(_.name != todo))
    }

    def complete(todo: String): IO[Unit] = async[IO] {
      val elements = !ref.todos
      val todoToComplete = elements.find(_.name == todo)
      todoToComplete.foreach {
        todo =>
          !ref.remove(todo.name)
          !ref.insert(todo.copy(status = LabelStatus.Done))
      }
    }