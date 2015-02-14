package messages

import akka.actor.ActorRef

object Dialog {
  val NotLoggedError = "You should be logged in to chat. Login, please."

  abstract class Request
  case class Login(username: String, ref: ActorRef) extends Request
  case class Logout(username: String) extends Request
  case class ChatMessage(username: String, message: String) extends Request
  case class PrivateMessage(from: String, to: String, message: String) extends Request
  case class GetChatLog(username: String) extends Request
  case class Connected(username: String) extends Request
  
  abstract class Response
  case class LoginError(error: String) extends Response
  case class LoginAck(username: String) extends Response
  case class ChatError(error: String) extends Response
  case class ChatLog(messages: List[(String, String)]) extends Response
  case class OnLine(users: List[String]) extends Response
}
