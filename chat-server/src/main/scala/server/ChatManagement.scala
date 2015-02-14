package server

import akka.actor.{Actor, ActorRef}
import messages.Dialog
import messages.Dialog._
import scala.language.postfixOps

object ChatManagement {

  case class Broadcast(message: Any, usernames: List[String])
}

/**
 * Chat message dispatching.
 * If messages from outside (remote) have to be sent
 * further to other actors of the system, they should always
 * be forwarded, such that the sender can be validated.
 */
trait ChatManagement {
  this: Actor =>

  import ChatManagement._

  def sessions: Map[String, ActorRef]

  protected def chatManagement: PartialFunction[Any, Unit] = {

    case message @ ChatMessage(username, _) =>
      forwardIfLogged(username, message)

    case message @ GetChatLog(username) =>
      forwardIfLogged(username, message)

    case message @ PrivateMessage(username, to, _) =>
      forwardPrivIfLogged(message)

    case message @ Connected(username) =>
      sender() ! OnLine(sessions.keys.toList)

    case Broadcast(message, usernames) if usernames isEmpty =>
      sessions foreach (s => s._2 ! message)

    case Broadcast(message, usernames) if usernames nonEmpty =>
      usernames foreach { name =>
        sessions.get(name).foreach(s => s ! message)
      }
  }

  private def forwardIfLogged(name: String, message: Any) = {
    sessions.get(name) match {
      case Some(session) =>
        session forward message
      case _ =>
        sender ! ChatError(Dialog.notLoggedError())
    }
  }

  private def forwardPrivIfLogged(message: PrivateMessage): Unit = {
    (sessions.get(message.from), sessions.get(message.to)) match {
      case (Some(from), Some(to)) =>
        from forward message

      case (None, Some(to)) =>
        sender() ! ChatError(Dialog.notLoggedError())

      case (Some(from), None) =>
        sender() ! ChatError(Dialog.notLoggedError(message.to))
      case _ =>
    }
  }
}
