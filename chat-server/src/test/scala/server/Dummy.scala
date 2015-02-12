package server

import akka.actor.Actor

class Dummy extends Actor {
  override def receive: Receive = {
    case _ => //do nothing
  }
}
