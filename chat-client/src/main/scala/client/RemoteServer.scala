package client

import akka.actor._
import client.ui.InputReader
import client.ui.InputReader.Exit
import messages.Dialog._

class RemoteServer(path: String) extends Actor with ActorLogging {

  private var username = ""
  private var reader: ActorRef = _

  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    context.actorSelection(path) ! Identify(path)

    import context.dispatcher

    import scala.concurrent.duration._
    context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  override def receive: Receive = identifying

  def identifying: Receive = {
    case ActorIdentity(p, Some(service)) =>
      context.watch(service)
      context.become(active(service))
      reader = context.actorOf(Props[InputReader])
    case ActorIdentity(p, None) =>
      log.info(s"Remote service not available at: $p")
    case ReceiveTimeout =>
      sendIdentifyRequest()
    case _ => log.info("RemoteServer: Unexpected message while identifying")
  }

  def active(service: ActorRef): Receive = {

    case msg: Request => msg match {
      case msg @ Login(name, ref) =>
        println(s"=== Logging in as $name ===")
        service ! msg

      case Logout(_) =>
        println(s"=== Logging out ===")
        service ! Logout(username)

      case ChatMessage(_, message) =>
        service ! ChatMessage(username, message)

      case PrivateMessage(_, to, message) =>
        service ! PrivateMessage(username, to, message)

      case msg @ Connected =>
        service ! Connected

      case GetChatLog(_) =>
        service ! GetChatLog(username)
    }

    case msg: Response => msg match {
        case LoginAck(name) =>
          username = name
          println(s"*** Logged in as $username")
          service ! GetChatLog(username)

        case LoginError(error) =>
          println(s"*** Error at login: $error")

        case ChatLog(list) =>
          println(s"=== Timeline ===")
          list foreach (msg => println(s"*** ${msg._1}: ${msg._2}"))

        case ChatError(error) =>
          println(s"*** Error: $error")

        case OnLine(list) =>
          println("=== Connected users ===")
          list foreach (user => println(s"*** $user"))

        case _ =>
          println("*** Unexpected response. Try again")

      }

    case Exit =>
      println("=== Closing chat ===")
      context.stop(self)
      context.system.shutdown()
      sys.exit()

    case Terminated(s) if s.equals(service) =>
      log.debug("Chat service terminated. Trying to reconnect ...")
      context.stop(reader)
      sendIdentifyRequest()
      context.become(identifying)

    case ReceiveTimeout => //ignore

    case m =>
      println(s"*** Strange, but the message didn't fit anything expected $m")
  }
}
