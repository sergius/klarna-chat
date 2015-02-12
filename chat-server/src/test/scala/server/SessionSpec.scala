package server

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SessionSpec extends TestKit(ActorSystem("SessionSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  override def afterAll(): Unit = system.shutdown()

  "When Session receives ChatMessage(from, message), it" must {

    "add the message to its internal list" in {

    }

    "send the message to storage" in {

    }
  }

}
