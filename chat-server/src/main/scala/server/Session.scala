package server

import akka.actor.{Actor, ActorLogging, ActorRef}
import messages.Dialog._
import server.SessionManagement.{CheckUser, UserNotPresent, UserPresent}

/**
 * Represents chat user session
 * @param userRef User's ActorRef
 * @param storage The actor responsible for persisting the messages
 */
class Session(val name: String, userRef: ActorRef, storage: ActorRef) extends Actor with ActorLogging{

  /**
   * A list of messages sent during the current session.
   * It could serve as an additional copy for the case
   * of failures in message storing and broadcasting,
   * though would need a more elaborate development not
   * implemented here.
   */
  private var messagesList = List.empty[String]

  def messages = messagesList

  private def isValid(sender: ActorRef): Boolean = {
    val valid = sender.equals(userRef)
    if (!valid) log.debug(s"Security: sender${sender.path} tried to send a message as ${userRef.path}")
    valid
  }

  override def receive: Receive = {

    case msg: ChatMessage =>
      if (isValid(sender())) {
        messagesList ::= msg.message
        storage ! msg
      }

    case msg: GetChatLog =>
      if (isValid(sender())) storage ! msg

    case msg: ChatLog =>
      filterPrivateAndSend(msg)

    case msg: PrivateMessage =>
      if (isValid(sender())) {
        storage ! msg
      }

    case CheckUser(ref) =>
      if (ref == userRef) {
        sender() ! UserPresent(self)
      }
      else sender() ! UserNotPresent
  }

  private def filterPrivateAndSend(message: ChatLog): Unit = {
    val filtered = message.messages.foldLeft(List.empty[(String, String)]) { (list, tuple) =>
      tuple match {
        case (username, msg) if username.startsWith("[") =>
          if (username.contains(name)) list :+ tuple
          else list
        case _ =>
          list :+ tuple
      }
    }
    userRef ! ChatLog(filtered)
  }
}
