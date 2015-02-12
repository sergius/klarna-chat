package server

import akka.actor._
import akka.testkit.{TestActorRef, ImplicitSender, TestKit, TestProbe}
import commons.Commons._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import persistence.{FileStorage, Persistence}
import server.Session.{Logout, Login, LoginAck, LoginError}

class SessionManagementSpec extends TestKit(ActorSystem("SessionManagementSpec"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  override def afterAll(): Unit = system.shutdown()

  "When SessionManagement receives Login(name) message, it" must {

    "should create a new entry in the sessionsMap if the name is valid" in {
      val user = "user"

      val sessionMngmnt = TestActorRef[SessionManagementTestImpl]

      sessionMngmnt ! Login(user)
      val sessionsKeys = sessionMngmnt.underlyingActor.sessions.keys
      sessionsKeys should have size 1
      sessionsKeys should contain(user)
    }

    "should respond with LoginError if the name already exists" in {
      val user = "user"
      val messageTester1 = TestProbe()
      val messageTester2 = TestProbe()

      val sessionMngmnt = system.actorOf(Props[SessionManagementTestImpl])

      messageTester1.send(sessionMngmnt, Login(user))
      messageTester1.expectMsg(LoginAck)

      messageTester2.send(sessionMngmnt, Login(user))
      messageTester2.expectMsgClass(classOf[LoginError])
    }
  }

  "When SessionManagement receives Logout(name) message, it" must {

    "remove the session from the sessionsMap" in {
      val user = "user"

      val sessionMngmnt = TestActorRef[SessionManagementTestImpl]

      sessionMngmnt ! Login(user)
      val sessionsKeys = sessionMngmnt.underlyingActor.sessions.keys
      sessionsKeys should have size 1
      sessionsKeys should contain(user)

      sessionMngmnt ! Logout(user)
      sessionMngmnt.underlyingActor.sessions shouldBe empty
    }
  }

}

class SessionManagementTestImpl
  extends SessionManagement
  with Persistence
  with Actor
  with ActorLogging{

  override def receive: Receive = sessionManagement

  override val storage: ActorRef = context.actorOf(Props(classOf[FileStorage], TestStorage))
}