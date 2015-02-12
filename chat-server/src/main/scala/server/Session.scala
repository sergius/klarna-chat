package server

import akka.actor.{Actor, ActorLogging, ActorRef}

object Session {
  sealed trait Event
  case class Login(name: String) extends Event
  case class LoginError(error: String) extends Event
  case object LoginAck extends Event
  case class Logout(name: String) extends Event

  case class ChatMessage(fromUser: String, message: String) extends Event

  case class GetChatLog(fromUser: String) extends Event
  case class ChatLog(messages: List[(String, String)]) extends Event
}

/**
 * Represents chat client session
 * @param user The username of the client
 * @param storage The actor responsible for persisting the messages
 */
class Session(user: String, storage: ActorRef) extends Actor with ActorLogging{
  import Session._

  private val loginTime = System.currentTimeMillis()

  /**
   * A list of messages sent during the current session.
   * It could serve as an additional copy for the case
   * of failures in message storing and broadcasting,
   * though would need a more elaborate development not
   * implemented here.
   */
  private var messagesList = List.empty[String]

  def messages = messagesList

  log.info(s"*** New Session for user $user has been created at $loginTime")

  override def receive: Receive = {
    case event: ChatMessage =>
      messagesList ::= event.message
      storage forward event

    case event: GetChatLog =>
      storage forward event
  }
}
