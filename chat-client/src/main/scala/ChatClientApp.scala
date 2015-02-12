import akka.actor.{Props, ActorSystem}
import client.ChatClient

object ChatClientApp extends App {

  def startRemoteLookupSystem(): Unit = {
    val system = ActorSystem("chat-client-system")
    val remotePath = "akka.tcp://chat-server-system@127.0.0.1:6667/user/chat-service-actor"
    val remoteServer = system.actorOf(Props(classOf[ChatClient], remotePath), "chat-client-actor")
    system.log.info("*** Chat client is starting up ...")
  }

  startRemoteLookupSystem()

}
