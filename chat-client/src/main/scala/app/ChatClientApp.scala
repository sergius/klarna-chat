package app

import akka.actor.{ActorSystem, Props}
import client.RemoteServer

object Conf {
  val path = "akka.tcp://chat-server-system@127.0.0.1:6667/user/chat-service-actor"
}

object ChatClientApp extends App {

  val system = ActorSystem("chat-client-system")
  val remoteServer = system.actorOf(Props(classOf[RemoteServer], Conf.path), "chat-client-actor")
  system.log.info("Chat client is starting up ...")
}
