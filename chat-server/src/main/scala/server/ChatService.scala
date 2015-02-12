package server

import akka.actor.{ActorRef, Props}
import persistence.Persistence

class ChatService(persistence: Props) extends ChatServer with Persistence {

  override val storage: ActorRef = context.actorOf(persistence, "chat-persistence-actor")
}
