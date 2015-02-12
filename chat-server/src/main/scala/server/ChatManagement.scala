package server

import akka.actor.ActorRef
import server.Session.{ChatMessage, GetChatLog}

/**
 * Chat message dispatching
 */
trait ChatManagement {
  this: ChatServer =>

  val sessions: Map[String, ActorRef]

  protected def chatManagement: PartialFunction[Any, Unit] = {

    case message @ ChatMessage(from, _) =>
      sessions(from) ! message

    case message @ GetChatLog(from) =>
      sessions(from) forward message
  }

}
