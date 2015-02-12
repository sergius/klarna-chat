package core

import akka.actor.{ActorRef, ActorSystem}

trait Core {

  implicit def system: ActorSystem
}

trait BootCore extends Core {

  implicit lazy val system = ActorSystem("chat-server-system")

  sys.addShutdownHook(system.shutdown())

  system.registerOnTermination {
    system.log.info("*** Chat Server System shut down")
  }
}

trait CoreActors {
  this: Core =>

  val server: ActorRef
}
