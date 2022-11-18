package it.unibo.example.game.shared

case class Todo(name: String, description: String, status: LabelStatus = LabelStatus.InProgress)
enum LabelStatus:
  case InProgress, Done