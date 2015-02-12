package server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import server.Session.{ChatLog, ChatMessage}

class ChatManagementSpec extends TestKit(ActorSystem("ChatManagementSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  override def afterAll(): Unit = system.shutdown()

  "When ChatManagement receives ChatMessage(from, message), it" must {
    "send the message to the corresponding session" in {
      val user1 = "user1"
      val messageTester1 = TestProbe()
      val user2 = "user2"
      val messageTester2 = TestProbe()
      val sessions: Map[String, ActorRef] =
        Map(user1 -> messageTester1.ref, user2 -> messageTester2.ref)

      val chatMngmnt = system.actorOf(Props(classOf[ChatManagementTestImpl], sessions))

      chatMngmnt ! ChatMessage(user1, "any")

      messageTester2.expectNoMsg()
      messageTester1.expectMsgClass(classOf[ChatMessage])
    }
  }

  "On receiving ChatLog(list), ChatManagement" must {
    "broadcast the chatLog to all connected users" in {
      val user1 = "user1"
      val messageTester1 = TestProbe()
      val user2 = "user2"
      val messageTester2 = TestProbe()
      val sessions: Map[String, ActorRef] =
        Map(user1 -> messageTester1.ref, user2 -> messageTester2.ref)

      val chatMngmnt = system.actorOf(Props(classOf[ChatManagementTestImpl], sessions))

      chatMngmnt ! ChatLog(List())

      messageTester1.expectMsgClass(classOf[ChatLog])
      messageTester2.expectMsgClass(classOf[ChatLog])
    }

  }

}

class ChatManagementTestImpl(sess: Map[String, ActorRef])
  extends ChatManagement with Actor {

  override val sessions: Map[String, ActorRef] = sess

  override def receive: Receive = chatManagement
}


