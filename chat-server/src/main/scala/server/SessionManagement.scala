package server

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.util.Timeout
import messages.Dialog._
import persistence.Persistence

import scala.concurrent.Future

object SessionManagement {

  case class CheckUser(user: ActorRef)
  sealed trait UserValidator
  case class UserPresent(session: ActorRef) extends UserValidator
  case object UserNotPresent extends UserValidator
  case class Duplicate(duplicateName: String, actualUser: ActorRef, actualSession: ActorRef)
}

/**
 * Users' session management
 */
trait SessionManagement {
  this: Actor with ActorLogging with Persistence =>

  import SessionManagement._

  private var sessionsMap = Map.empty[String, ActorRef] // username -> session
  def sessions = sessionsMap

  protected def sessionManagement: PartialFunction[Any, Unit] = {

    case Login(name, ref) =>
      sessionsMap.get(name) match {
        case Some(s) =>
          sender() ! LoginError(s"Name $name already in use")
        case _ =>
          verifyDuplicateSession(name, ref)
          val session = context.actorOf(Props(classOf[Session], name, ref, storage))
          sessionsMap += name -> session
          log.info(s"User $name logged in")
          sender() ! LoginAck(name)
      }

    case Logout(username) =>
      closeSession(username)

    case Duplicate(username, user, session) =>
      user ! LoginError(s"The session for $username is a duplicate one and will be closed")
      closeSession(username)
      resendActualName(user, session)
      log.debug(s"Duplicate: session for $username is closed")

    case UserNotPresent =>
    // do nothing
  }

  def resendActualName(user: ActorRef, session: ActorRef): Unit = {
    sessions.find(el => el._2 == session).foreach(el => user ! LoginAck(el._1))
  }

  private def closeSession(username: String): Unit = {
    sessionsMap.get(username) match {
      case Some(session) =>
        context.stop(session)
        sessionsMap -= username
        log.info(s"User $username logged out")
      case _ =>
        log.debug(s"User $username not found when trying to logout")
    }
  }

  private def verifyDuplicateSession(username: String, user: ActorRef): Unit = {
    import akka.pattern.pipe
    import context.dispatcher
    import akka.pattern.ask
    import scala.concurrent.duration._
    implicit val timeout = Timeout(3.seconds)

    Future.sequence(sessions.values.map { s =>
      (s ? CheckUser(user)).mapTo[UserValidator]
    }) map { results =>
      val duplicates = results.foldLeft(List.empty[ActorRef]) { (list, r) =>
        r match {
          case UserPresent(session) => session :: list
          case _ => list
        }
      }
      if (duplicates.nonEmpty) Duplicate(username, user, duplicates.head)
      else UserNotPresent
    } pipeTo self
  }
}
