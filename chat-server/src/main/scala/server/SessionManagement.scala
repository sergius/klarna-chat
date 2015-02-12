package server

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import persistence.Persistence
import server.Session.{LoginAck, LoginError, Login, Logout}

/**
 * Users' session management
 */
trait SessionManagement {
  this: Actor with ActorLogging with Persistence =>

  private var sessionsMap = Map.empty[String, ActorRef] // username -> session
  def sessions = sessionsMap

  protected def sessionManagement: PartialFunction[Any, Unit] = {
    case Login(username) =>
      sessionsMap.get(username) match {
        case Some(s) =>
          sender() ! LoginError("Username already in use")
        case _ =>
          log.info(s"*** User $username has logged in")
          val session = context.actorOf(Props(classOf[Session], username, storage))
          sessionsMap += username -> session
          sender() ! LoginAck
      }

    case Logout(username) =>
      log.info(s"*** User $username has logged out")
      context.stop(sessionsMap(username))
      sessionsMap -= username
  }
}
