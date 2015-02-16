package client.ui

import akka.actor.Actor
import messages.Dialog._

import scala.io.StdIn

object InputReader {
  case object Start
  case object Exit
}

class InputReader extends Actor with Terminal {
  import InputReader._

  override def receive: Receive = {

    case Start =>
      commandLoop()
  }

  override val commandParser: CommandParser.Parser[Command] =
    CommandParser.exit | CommandParser.login |
      CommandParser.logout | CommandParser.connected | CommandParser.help |
      CommandParser.status | CommandParser.privateMsg| CommandParser.talk


  val remoteServer = context.parent

  println(s"=== Starting Chat Client App ===\n")
  printHelp()
  commandLoop()

  def printHelp(): Unit = {
    println("=== Please use the following formats for commands: ===\n\n" +
      "1. 'login Batman' - to login as Batman\n\n" +
      "(*note: the rest of commands start with '$\\'*)\n\n" +
      "2. '$\\help' - to see these instructions\n\n" +
      "3. '$\\logout' - to logout from chat\n\n" +
      "4. '$\\exit' - to exit this application\n\n" +
      "5. '$\\private John It's a secret' - to write a private message to John\n\n" +
      "6. '$\\connected' - to see all connected users\n\n" +
      "7. '$\\status' - to see a refreshed chat timeline\n\n" +
      "Please remember: when logged-in, whatever you type, \n" +
      "if it's not one of the mentioned commands, it will be treated\n" +
      " as a message and will be published to the chat.\n\n"
    )
  }

  def commandLoop(): Unit = {
    import Command._

    Command(StdIn.readLine()) match {
      case Quit =>
        remoteServer ! Exit
        commandLoop()

      case Join(name) =>
        remoteServer ! Login(name, remoteServer)
        commandLoop()

      case OnLineUsers =>
        remoteServer ! Connected
        commandLoop()

      case Leave =>
        remoteServer ! Logout("")
        commandLoop()

      case PrivateMsg(name, message) =>
        remoteServer ! PrivateMessage("", name, message)
        commandLoop()

      case Status =>
        remoteServer ! GetChatLog("")
        commandLoop()

      case Talk(message) =>
        remoteServer ! ChatMessage("", message)
        commandLoop()

      case Help =>
        printHelp()
        commandLoop()

      case Unknown(s, m) =>
        println(s"*** Unexpected command: $s. Cause: $m")
        printHelp()
        commandLoop()
    }
  }
}
