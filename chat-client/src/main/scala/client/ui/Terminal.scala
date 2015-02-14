package client.ui

import scala.util.parsing.combinator.RegexParsers

trait Terminal {
  import Command._

  sealed trait Command

  object Command {

    def apply(command: String): Command =
      CommandParser.parseAsCommand(command)

    case class Unknown(command: String, message: String) extends Command
    case object Quit extends Command
    case object Status extends Command
    case class Join(name: String) extends Command
    case object Leave extends Command
    case class PrivateMsg(to: String, message: String) extends Command
    case object OnLineUsers extends Command
    case class Talk(message: String) extends Command
    case object Help extends Command
  }

  val commandParser: CommandParser.Parser[Command]

  object CommandParser extends RegexParsers {

    def parseAsCommand(s: String): Command = {
      parseAll(commandParser, s) match {
        case Success(command, _) => command
        case NoSuccess(message, _) => Unknown(s, message)
      }
    }


    def login: Parser[Command] = ("login".r ~ word) ^^ {
      case _ ~ username => Join(username)
    }

    def talk: Parser[Command] = phrase ^^ (ph => Talk(ph))

    def exit: Parser[Command] = (prefix ~ "exit".r) ^^ (_ => Quit)

    def help: Parser[Command] = (prefix ~ "help".r) ^^ (_ => Help)

    def status: Parser[Command] = (prefix ~ "status".r) ^^ (_ => Status)

    def connected: Parser[Command] = (prefix ~ "connected".r) ^^ (_ => OnLineUsers)

    def logout: Parser[Command] = (prefix ~ "logout".r) ^^ (_ => Leave)

    def privateMsg: Parser[Command] = (prefix ~ "private".r ~ word ~ phrase) ^^ {
      case _ ~ _ ~ to ~ msg => PrivateMsg(to, msg)
    }


    def word: Parser[String] = """[^\s]+""".r ^^ (_.trim)

    def phrase: Parser[String] = """.+""".r ^^ (_.trim)

    def prefix: Parser[String] = """\$\\""".r ^^ (_.trim)

  }
}
