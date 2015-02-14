package persistence

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import commons.Commons._
import messages.Dialog.{ChatLog, ChatMessage, GetChatLog}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.io.Source

class FileStorageSpec extends TestKit(ActorSystem("FileStorageSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfter
with BeforeAndAfterAll {

  private def emptyTestFileStorage(): Any = {
    Files.write(Paths.get(TestStorage), "".getBytes)
  }

  before{
    emptyTestFileStorage()
  }

  override def afterAll(): Unit = system.shutdown()


  
  "When FileStorage receives ChatMessage(from, message), it" must {

    "update existing chatLog" in {
      val testUser = "test"
      val testMsg = "message"
      val storageRef = TestActorRef(new FileStorage(TestStorage))
      val storageActor = storageRef.underlyingActor

      storageActor.chatLogAgent() shouldBe empty

      storageRef ! ChatMessage(testUser, testMsg)

      storageActor.chatLogAgent() should have size 1
      storageActor.chatLogAgent() should contain((testUser, testMsg))
    }

    "persist it to the file" in {
      val testUser = "test"
      val testMsg = "message"
      val storageRef = TestActorRef(new FileStorage(TestStorage))

      Source.fromFile(TestStorage).getLines().toList shouldBe empty

      storageRef ! ChatMessage(testUser, testMsg)

      Thread.sleep(10) //give some time to write to file
      val lines: List[String] = Source.fromFile(TestStorage).getLines().toList
      lines should have size 1
      lines should contain(testUser + FileStorage.Separator + testMsg)
    }

    "return updated chatLog to the sender of ChatMessage (i.e. to ChatManagement)" in {
      val messages = List(("test1", "msg1"), ("test2", "msg2"), ("test3", "msg3"))
      val storage = TestActorRef(new FileStorage(TestStorage))
      val messageTester = TestProbe()

      messages foreach { m =>
        messageTester.send(storage, ChatMessage(m._1, m._2))
      }

      messageTester.expectMsg(ChatLog(messages.take(1)))
      messageTester.expectMsg(ChatLog(messages.take(2).reverse))
      messageTester.expectMsg(ChatLog(messages.reverse))
    }
  }

  "When receiving the GetChatLog message, FileStorage" must {

    "send ChatLog to the sender" in {
      val messages = List(("test1", "msg1"), ("test2", "msg2"), ("test3", "msg3"))
      val messageTester = TestProbe()
      val storage = TestActorRef(new FileStorage(TestStorage))

      messages foreach { m =>
        storage ! ChatMessage(m._1, m._2)
      }

      messageTester.send(storage, GetChatLog("any"))
      messageTester.expectMsg(ChatLog(messages.reverse))
    }
  }

}
