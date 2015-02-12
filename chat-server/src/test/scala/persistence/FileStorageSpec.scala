package persistence

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class FileStorageSpec extends TestKit(ActorSystem("FileStorageSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  override def afterAll(): Unit = system.shutdown()
  
  "When FileStorage receives ChatMessage(from, message), it" must {

    "update existing chatLog" in {

    }

    "persist it to the file" in {

    }

    "return updated chatLog to the sender of ChatMessage (i.e. ChatManagement)"
  }

}
