package persistence

import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorLogging, Props}
import akka.agent.Agent
import server.Session.{ChatLog, GetChatLog}

import scala.io.BufferedSource
import scala.util.{Failure, Success, Try}


object FileStorage {

  val FileName = "file-storage.txt"
  val Separator = ":"

  case class Persist(chatLog: List[(String, String)])
}

/**
 * Actor responsible to store the chat log.
 * Note: possible storage errors will not be handled
 * in this simple application.
 */
class FileStorage extends Actor with ActorLogging {
  import context._
  import persistence.FileStorage._
  import server.Session.ChatMessage

  lazy val chatLogAgent = Agent(chatLog)

  private lazy val chatLog: List[(String, String)] = Try(io.Source.fromFile("file-storage.txt")) match {
    case Success(source: BufferedSource) =>
      source.getLines().toList.map{l =>
        val line = l.split(Separator)
        (line(0), line(1))}
    case _ =>
      List.empty[(String, String)]
  }

  override def receive: Receive = {
    case ChatMessage(from, message) =>
      log.info(s"*** New chat message: $message")
      chatLogAgent alter ((from, message) :: _) onComplete {
        case Success(_) =>
          context.actorOf(Props[Persister]) ! Persist(chatLogAgent())
        case _ => // omit handling it in this simple application
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
class Persister extends Actor with ActorLogging {
  import persistence.FileStorage._

  override def receive: Actor.Receive = {
    case Persist(list) =>
      val txt = list.foldLeft("")((acc, s) => acc + s"\n $s")
      Try(Files.write(Paths.get(FileName), txt.getBytes)) match {
        case Success(_) =>
          context.stop(self)
        case Failure(e) => log.debug(s"*** Could not store chat log to file: $e")
      }
  }
}
