package server

import akka.actor.{Actor, ActorRef}
import messages.Dialog
import messages.Dialog._

/**
 * Chat message dispatching.
 * If messages from outside (remote) have to be sent
 * further to other actors of the system, they should always
 * be forwarded, such that the sender can be validated.
 */
trait ChatManagement {
  this: Actor =>

  def sessions: Map[String, ActorRef]

  private def forwardIfLogged(name: String, message: Any) = {
    sessions.get(name) match {
      case Some(session) =>
        session forward message
      case _ =>
        sender() ! ChatError(Dialog.NotLoggedError)
    }
  }

  protected def chatManagement: PartialFunction[Any, Unit] = {

    case message @ ChatMessage(username, _) =>
      forwardIfLogged(username, message)

    case message @ GetChatLog(username) =>
      forwardIfLogged(username, message)

    case message @ ChatLog(_) =>
      sessions foreach { s =>
        s._2 ! message
      }

    case message @ PrivateMessage(username, to, _) =>
      forwardIfLogged(username, message)
      forwardIfLogged(to, message)

    case message @ Connected(username) =>
      sender() ! OnLine(sessions.keys.toList)
  }

}
