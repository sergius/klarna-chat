package server

import akka.actor.{Actor, ActorRef}
import server.Session.{ChatLog, ChatMessage, GetChatLog}

/**
 * Chat message dispatching
 */
trait ChatManagement {
  this: Actor =>

  val sessions: Map[String, ActorRef]

  protected def chatManagement: PartialFunction[Any, Unit] = {

    case message @ ChatMessage(username, _) =>
      sessions(username) ! message

    case message @ GetChatLog(username) =>
      sessions(username) forward message

    case message @ ChatLog(list) =>
      sessions foreach { s =>
        s._2 ! message
      }
  }

}
