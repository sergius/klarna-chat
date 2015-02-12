package server

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SessionManagementSpec extends TestKit(ActorSystem("SessionManagementSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  override def afterAll(): Unit = system.shutdown()

  "When SessionManagement receives Login(name) message, it" must {

    "should fail if the name already exists" in {

    }

    "should create a new entry in the sessionsMap" in {

    }


  }

  "When SessionManagement receives Logout(name) message, it" must {

    "remove the session from the sessionsMap" in {

    }
  }

}
