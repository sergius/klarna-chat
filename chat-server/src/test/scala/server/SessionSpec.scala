package server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import messages.Dialog.{ChatLog, ChatMessage}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SessionSpec extends TestKit(ActorSystem("SessionSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  override def afterAll(): Unit = system.shutdown()

  "When Session receives ChatMessage(from, message), it" must {

    "when the sender is valid, add the message to its internal list" in {
      val testUser = "test"
      val testMsg = "message"
      val sender = TestProbe()
      val sessionRef = TestActorRef(new Session(sender.ref, system.actorOf(Props[Dummy])))
      val sessionActor = sessionRef.underlyingActor

      sessionActor.messages shouldBe empty

      sender.send(sessionRef, ChatMessage(testUser, testMsg))

      sessionActor.messages should have size 1
      sessionActor.messages should contain(testMsg)
    }

    "when the sender is invalid, omit the message" in {
      val testUser = "test"
      val testMsg = "message"
      val sender = TestProbe()
      val sessionRef = TestActorRef(new Session(sender.ref, system.actorOf(Props[Dummy])))
      val sessionActor = sessionRef.underlyingActor

      sessionActor.messages shouldBe empty

      sessionRef ! ChatMessage(testUser, testMsg)

      sessionActor.messages shouldBe empty
    }

    "when the sender is valid, forward the message to storage" in {
      val testUser = "test"
      val testMsg = "message"
      val sender = TestProbe()
      val storage = TestProbe()
      val sessionRef = TestActorRef(new Session(sender.ref, storage.ref))

      val msg = ChatMessage(testUser, testMsg)
      sender.send(sessionRef, msg)

      storage.expectMsg(msg)
    }
  }

  "On receiving ChatLog, Session" must {
    "send it to the corresponding user" in {
      val testUser = "test"
      val testMsg = "message"
      val sender = TestProbe()
      val sessionRef = TestActorRef(new Session(sender.ref, system.actorOf(Props[Dummy])))

      val msg: ChatLog = ChatLog(List((testUser, testMsg)))
      sessionRef ! msg

      sender.expectMsg(msg)
    }
  }

}
