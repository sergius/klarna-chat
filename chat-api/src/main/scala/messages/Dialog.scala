package messages

import akka.actor.ActorRef

object Dialog {

  def notLoggedError(user: String = "You"): String =
    s"Not logged. $user should log-in to start chatting."

  abstract class Request
  case class Login(username: String, ref: ActorRef) extends Request
  case class Logout(username: String) extends Request
  case class ChatMessage(username: String, message: String) extends Request
  case class PrivateMessage(from: String, to: String, message: String) extends Request
  case class GetChatLog(username: String) extends Request
  case object Connected extends Request
  
  abstract class Response
  case class LoginError(error: String) extends Response
  case class LoginAck(username: String) extends Response
  case class ChatError(error: String) extends Response
  case class ChatLog(messages: List[(String, String)]) extends Response
  case class OnLine(users: List[String]) extends Response
}
