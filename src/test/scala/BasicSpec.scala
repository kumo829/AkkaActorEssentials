import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec")) with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  "A Simple Actor" should {
    "send back the same message" in {
      val simpleActor = system.actorOf(Props[SimpleActor])
      val message = "Hello, Test"
      simpleActor ! message

      expectMsg(message)
    }
  }

  "A BlackHole Actor" should {
    "send back some message" in {
      val blackHole = system.actorOf(Props[BlackHole])
      val message = "Hello, Test"
      blackHole ! message

      expectNoMessage(1 second)
    }
  }

  "A Lab\test Actor" should {

    val labTest = system.actorOf(Props[LabTestActor])

    "turn a string into uppercase" in {
      labTest ! "I love Akka!"
      val reply = expectMsgType[String]

      assert(reply == "I LOVE AKKA!")
    }

    "reply to a greeting" in {
      labTest ! "greeting"
      expectMsgAnyOf("hi!", "hello!")
    }

    "reply with favorite tech" in {
      labTest ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply cool tech in a different way" in {
      labTest ! "favoriteTech"
      val messages = receiveN(2)
    }

    "reply with a cool tech in a fancy way" in {
      labTest ! "favoriteTech"

      expectMsgPF() {
        case "Scala" =>
        case "Akka" =>
      }
    }
  }
}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {

    val random = new Random()

    override def receive: Receive = {
      case "greeting" => sender() ! (if(random.nextBoolean()) "hi!" else "hello!")
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase
    }
  }
}