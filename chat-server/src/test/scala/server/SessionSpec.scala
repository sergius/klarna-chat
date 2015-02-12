package server

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestProbe, TestActorRef, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import server.Session.ChatMessage

class SessionSpec extends TestKit(ActorSystem("SessionSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  override def afterAll(): Unit = system.shutdown()

  "When Session receives ChatMessage(from, message), it" must {

    "add the message to its internal list" in {
      val testUser = "test"
      val testMsg = "message"
      val sessionRef = TestActorRef(new Session("any", system.actorOf(Props[Dummy])))
      val sessionActor = sessionRef.underlyingActor

      sessionActor.messages shouldBe empty

      sessionRef ! ChatMessage(testUser, testMsg)

      sessionActor.messages should have size 1
      sessionActor.messages should contain(testMsg)
    }

    "forward the message to storage" in {
      val testUser = "test"
      val testMsg = "message"
      val messageTester = TestProbe()
      val sessionRef = TestActorRef(new Session("any", messageTester.ref))

      sessionRef ! ChatMessage(testUser, testMsg)

      messageTester.expectMsg(ChatMessage(testUser, testMsg))
    }
  }

}
