package client

import akka.actor.Status.Success
import akka.actor._
import server.Session.{ChatLog, ChatMessage, Logout, Login}

class ChatClient(path: String) extends Actor with ActorLogging {

  private var username: Option[String] = None

  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    context.actorSelection(path) ! Identify(path)

    import context.dispatcher
    import scala.concurrent.duration._
    context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  override def receive: Receive = identifying

  def identifying: Receive = {
    case ActorIdentity(path, Some(server)) =>
      context.watch(server)
      context.become(active(server))
    case ActorIdentity(path, None) =>
      log.info(s"*** Remote server not available at: $path")
    case ReceiveTimeout =>
      sendIdentifyRequest()
    case _ => log.info("*** ChatClient: Unexpected message while identifying")
  }

  def active(server: ActorRef): Receive = {
/*    case msg @ Login(name) =>
      server ! msg
    case msg @ Logout(name) =>
      server ! msg
    case Post(_) if username == None =>
      sender ! ChatError("You should login first")
    case Post(message) =>
      server ! ChatMessage(username.get, message) //TODO Refactor it
    case Status =>
      import akka.pattern.ask
      val s = sender
      server ? ChatLog onComplete {
        case Success(chatLog) =>
      }*/
    case Terminated(_) =>
      log.info("*** Chat server terminated")
      sendIdentifyRequest()
      context.become(identifying)
    case ReceiveTimeout => //ignore
  }
}
