package persistence

import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit._
import commons.Commons._
import messages.Dialog.{ChatLog, ChatMessage, GetChatLog}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import server.ChatManagement.Broadcast

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

      storageActor.chatRecord shouldBe empty

      storageRef ! ChatMessage(testUser, testMsg)

      storageActor.chatRecord should have size 1
      storageActor.chatRecord should contain((testUser, testMsg))
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

    "On receiving ChatMessage, send updated chatLog for Broadcast to parent" in {
      val messages = List(("test1", "msg1"), ("test2", "msg2"), ("test3", "msg3"))
      val messageTester = TestProbe()

      class Mock extends Actor {
        override def receive: Receive = {
          case msg @ _ => messageTester.ref forward msg
        }
        val storage = context.actorOf(Props(classOf[FileStorage], TestStorage))
      }
      val parent = TestActorRef(new Mock)
      val storage: ActorRef = parent.underlyingActor.storage

      val el1 = messages.head
      storage ! ChatMessage(el1._1, el1._2)
      messageTester.expectMsg(Broadcast(ChatLog(messages.take(1)), List()))

      val el2 = messages.drop(1).head
      storage ! ChatMessage(el2._1, el2._2)
      messageTester.expectMsg(Broadcast(ChatLog(messages.take(2).reverse), List()))

      val el3 = messages.drop(2).head
      storage ! ChatMessage(el3._1, el3._2)
      messageTester.expectMsg(Broadcast(ChatLog(messages.reverse), List()))
    }
  }

  "When receiving the GetChatLog message, FileStorage" must {

    "send ChatLog to its parent" in {
      val testSender = "any"
      val messages = List(("test1", "msg1"), ("test2", "msg2"), ("test3", "msg3"))
      val messageTester = TestProbe()

      class Mock extends Actor {
        override def receive: Receive = {
          case msg @ _ => messageTester.ref forward msg
        }
        val storage = context.actorOf(Props(classOf[FileStorage], TestStorage))
      }

      val parent = TestActorRef(new Mock)
      val storage: ActorRef = parent.underlyingActor.storage
      messages foreach { m =>
        storage ! ChatMessage(m._1, m._2)
      }

      storage ! GetChatLog(testSender)

      import scala.concurrent.duration._
      messageTester.fishForMessage (3.seconds){
        case m: Any => m == Broadcast(ChatLog(messages.reverse), List(testSender))
      }
    }
  }

}
