package server

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor._
import persistence.Persistence
import scala.language.postfixOps

import scala.concurrent.duration._

trait ChatServer extends Actor with ActorLogging with SessionManagement with ChatManagement {
  this: Persistence =>

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case _: ActorInitializationException  => Stop
    case _: ActorKilledException => Stop
    case _: Exception => Restart
    case _ => Escalate
  }

  log.info("*** Chat server is starting up...")

  def receive = sessionManagement orElse chatManagement

  protected def chatManagement: PartialFunction[Any, Unit]
  protected def sessionManagement: PartialFunction[Any, Unit]
}
