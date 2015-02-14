package persistence

import java.nio.file.{StandardOpenOption, Files, Paths}

import akka.actor.{Actor, ActorLogging, Props}
import akka.agent.Agent
import messages.Dialog.{GetChatLog, ChatLog, ChatMessage}

import scala.io.{Source, BufferedSource}
import scala.util.{Failure, Success, Try}


object FileStorage {
  val Separator = ":"

  case class Persist(name: String, message: String)
}

/**
 * Actor responsible to store the chat log.
 * Note: possible storage errors will not be handled
 * in this simple application.
 * When receiving ChatMessage returns full chatLog to the sender
 * with all last updates, so they can be broadcast to all connected
 * users. The broadcast is always full (not incremental) for certain
 * reasons; please, see Readme for details.
 */
class FileStorage(val filePath: String) extends Actor with ActorLogging {
  import context._
  import persistence.FileStorage._

  private val chatLog: List[(String, String)] = Try(Source.fromFile(filePath)) match {
    case Success(source: BufferedSource) =>
      source.getLines().toList.filter(el => el.trim.length > 0) match {
        case list @ el :: rest =>
          list.map{l =>
            val line = l.split(Separator)
            (line(0), line(1))}
        case List() =>
          List.empty[(String, String)]
      }
    case _ =>
      List.empty[(String, String)]
  }

  val chatLogAgent = Agent(chatLog)

  override def receive: Receive = {
    case ChatMessage(user, message) =>
      val userRef = sender()
      chatLogAgent alter ((user, message) :: _) onComplete {
        case Success(_) =>
          userRef ! ChatLog(chatLogAgent())
          context.actorOf(Props(classOf[Persister], filePath)) ! Persist(user, message)
        case _ =>
          log.debug(s"There was a problem writing to file $filePath")
        // omit handling it in this simple application
      }
    case GetChatLog(_) =>
      sender ! ChatLog(chatLogAgent())
  }
}

/**
 * A worker that stores the message to file.
 * Writes to file and stops.
 * Note: possible storage errors will not be handled
 * in this simple application.
 */
class Persister(val filePath: String) extends Actor with ActorLogging {
  import persistence.FileStorage._

  override def receive: Actor.Receive = {
    case Persist(name, message) =>
      val txt = name + Separator + message + "\n"
      Try(Files.write(Paths.get(filePath), txt.getBytes, StandardOpenOption.APPEND)) match {
        case Success(_) =>
          log.debug(s"Entry ($name, $message) successfully persisted to $filePath")
          context.stop(self)
        case Failure(e) =>
          log.debug(s"Could not store chat log to file: $e")
      }
  }
}
