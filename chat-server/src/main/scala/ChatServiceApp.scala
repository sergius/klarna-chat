import akka.actor.{Props, ActorRef}
import core.{BootCore, CoreActors}
import persistence.FileStorage
import server.ChatService

object ChatServiceApp extends App with BootCore with CoreActors {

  override val server: ActorRef =
    system.actorOf(Props(classOf[ChatService], Props[FileStorage]), "chat-service-actor")
}
