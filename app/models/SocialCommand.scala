package models

import models.SocialCommand.Command

import scala.util.matching.Regex

case class SocialCommand(command: Command, value: String)

object SocialCommand {

  sealed trait Command

  case object Follow extends Command

  case object Unfollow extends Command

  val all = Seq(Follow, Unfollow)

  def fromString(value: String): Option[Command] = {
    all.find(_.toString.toLowerCase == value.toLowerCase)
  }

  def processCommand(message: String, pattern: Regex): Option[SocialCommand] = {
    message match {
      case pattern(command, value) =>
        fromString(command).map(SocialCommand(_, value)).orElse(None)
      case _ => None
    }
  }

}