package app

import akka.actor.{ActorRef, Props}
import core.{BootCore, CoreActors}
import persistence.FileStorage
import server.ChatService

object ChatServiceApp extends BootCore with CoreActors with App {

  val filePath = getClass.getResource("/file-storage").getPath

  override val server: ActorRef =
    system.actorOf(Props(classOf[ChatService], Props(classOf[FileStorage], filePath)), "chat-service-actor")
}
