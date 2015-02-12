package persistence

import akka.actor.ActorRef

trait Persistence {

  val storage: ActorRef
}
