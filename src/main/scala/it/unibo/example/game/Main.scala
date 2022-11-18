package it.unibo.example.game

import cats.effect.cps.*
import cats.effect.IO
import it.unibo.cats.dsl.*
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole

import cats.effect.kernel.Ref
import scala.concurrent.ExecutionContext.Implicits.global as futureEC
import cats.effect.unsafe.implicits.global as ioRuntime
import it.unibo.example.game.shared.{LabelStatus, Todo, Whiteboard}
import it.unibo.example.game.shared.Whiteboard.*
import org.fusesource.jansi.Ansi.ansi

import scala.util.{Random, Try}

@main def main(): Unit = async[IO] {
  !IO(AnsiConsole.systemInstall())
  val whiteboard = !Whiteboard()

  def loop: IO[Unit] = async[IO] {
    !welcome
    !menu
    val status = !handleInput(whiteboard)
    if(status == ApplicationStatus.Continue)
      !loop
  }

  !loop
}.unsafeRunSync()

def welcome = async[IO] {!IO.println(ansi().eraseScreen().render("@|green,bold Hello to a simple Todo manager application! |@")) }
def menu = async[IO] {
  !IO.println(ansi().render("@|bold,yellow Please choose what you wanna to do|@"))
  !IO.println(ansi().render("@|bold 1.|@ show todos"))
  !IO.println(ansi().render("@|bold 2.|@ insert a todo"))
  !IO.println(ansi().render("@|bold q.|@ exit to the application"))
}

def handleInput(whiteboard: Whiteboard.State): IO[ApplicationStatus] = async[IO] {
  val input = !IO.readLine
  input match
    case "1" => !todosDisplay(whiteboard)
    case "2" => !insertTodo(whiteboard)
    case "q" => !IO(ApplicationStatus.End)
    case _ => !IO.println(ansi().render("@|bold,red Wrong selection!! |@"))
      !handleInput(whiteboard)
}

def todosDisplay(whiteboard: Whiteboard.State): IO[ApplicationStatus] = async[IO] {
  val todos = !whiteboard.todos
  todos.foreach(!renderTodo(_))
  !IO.println("Select one todo (name) in order to get the description, press enter to come back to the menu")
  val name = !IO.readLine
  if(name.nonEmpty)
    val todoSelected = !whiteboard.get(name)
    !detailsOf(todoSelected, whiteboard)
  ApplicationStatus.Continue
}

def detailsOf(todo: Todo, whiteboard: Whiteboard.State) = async[IO] {
  !IO.println(ansi().eraseScreen().bgCyan().bold().a(todo.name).boldOff().bgDefault())
  !IO.println(ansi().bgBrightGreen().a(todo.description).bgDefault())
  !IO.println(ansi().bgDefault().render("You wanna complete it? y/N"))
  val decision = !IO.readLine
  decision match
    case "y" => !whiteboard.complete(todo.name)
    case _ =>
}

def renderTodo(todo: Todo): IO[Unit] =
  todo.status match
    case LabelStatus.InProgress => IO.println(ansi().render(s"@|bold,red ${todo.name} |@"))
    case LabelStatus.Done => IO.println(ansi().render(s"@|green ${todo.name}|@"))

def insertTodo(whiteboard: Whiteboard.State): IO[ApplicationStatus] = async[IO] {
  !IO.println("Insert a name for the todo")
  val name = !IO.readLine
  !IO.println("Insert the description")
  val description = !IO.readLine
  !whiteboard.insert(Todo(name, description))
  ApplicationStatus.Continue
}

enum ApplicationStatus:
  case Continue, End
