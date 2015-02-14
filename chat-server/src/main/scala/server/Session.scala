package server

import akka.actor.{Actor, ActorLogging, ActorRef}
import messages.Dialog._
import server.SessionManagement.{UserNotPresent, UserPresent, CheckUser}

/**
 * Represents chat client session
 * @param user The username of the client
 * @param storage The actor responsible for persisting the messages
 */
class Session(val user: ActorRef, storage: ActorRef) extends Actor with ActorLogging{

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
    val valid = sender.equals(user)
    if (!valid) log.debug(s"Security: sender${sender.path} tried to send a message as ${user.path}")
    valid
  }

  override def receive: Receive = {

    case msg: ChatMessage =>
      if (isValid(sender())) {
        messagesList ::= msg.message
        storage forward msg
      }

    case msg: GetChatLog =>
      if (isValid(sender())) storage forward msg

    case msg @ ChatLog(list) =>
      user ! msg

    case msg: PrivateMessage =>
      //TODO Validity is different

    case CheckUser(ref) =>
      if (ref == user) {
        sender() ! UserPresent(self)
      }
      else sender() ! UserNotPresent
  }
}
