package server

import akka.actor.{ActorRef, Props}
import persistence.Persistence
import server.Session.{Login, Logout}

/**
 * Users' session management
 */
trait SessionManagement {
  this: ChatServer with Persistence =>

  private var sessionsMap = Map.empty[String, ActorRef] // username -> session
  val sessions = sessionsMap

  protected def sessionManagement: PartialFunction[Any, Unit] = {
    case Login(username) =>
      log.info(s"*** User $username has logged in")
      val session = context.actorOf(Props(classOf[Session], username, storage))
      sessionsMap += username -> session

    case Logout(username) =>
      log.info(s"*** User $username has logged out")
      context.stop(sessionsMap(username))
      sessionsMap -= username
  }
}
