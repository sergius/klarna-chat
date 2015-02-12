package server

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ChatManagementSpec extends TestKit(ActorSystem("ChatManagementSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  override def afterAll(): Unit = system.shutdown()

  "When ChatManagement receives ChatMessage(from, message), it" must {

    "send the message to the corresponding session" in {

    }

    ""
  }

  "On receiving ChatLog(list), ChatManagement" must {

    "broadcast the chatLog to all connected users" in {

    }
  }

}
