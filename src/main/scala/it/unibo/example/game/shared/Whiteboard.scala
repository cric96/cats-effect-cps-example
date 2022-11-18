package it.unibo.example.game.shared
import cats.effect
import cats.effect.IO
import cats.effect.cps.*
import it.unibo.cats.dsl.*
import cats.effect.kernel.Ref

object Whiteboard:
  // type alias for devising the state of a whiteboard
  type State = Ref[IO, List[Todo]]
  /**
   * Create the whiteboard, i.e, a shared placed in which it is possible to add todos
   * @return 
   */
  def apply(): IO[State] = async[IO] { !IO.ref(List.empty[Todo]) }

  /**
   * extension method that you can call in the Whiteboard state
   */
  extension(state: State)
    def todos: IO[List[Todo]] = async[IO] { !state.get }

    def get(name: String): IO[Todo] = async[IO] {
      val todo = state.todos.await.find(_.name == name)
      !IO.fromOption(todo)(IllegalArgumentException(s"No ${name} in the whiteboard"))
    }

    def insert(todo: Todo): IO[Unit] = async[IO] { !state.update(todos => todo :: todos) }

    def remove(todo: String): IO[Unit] = async[IO] { !state.update(todos => todos.filter(_.name != todo)) }

    def complete(todo: String): IO[Unit] = async[IO] {
      val elements = !state.todos
      val todoToComplete = elements.find(_.name == todo)
      todoToComplete.foreach {
        todo =>
          !state.remove(todo.name)
          !state.insert(todo.copy(status = LabelStatus.Done))
      }
    }