package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifecycle extends App {
  object StartChild

  class LifecycleActor extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("I am starting")

    override def postStop(): Unit = log.info("I have stopped")

    override def receive: Receive = {
      case StartChild => context.actorOf(Props[LifecycleActor], "child")
    }
  }

  val system = ActorSystem("LifecycleDemo")
  val parent = system.actorOf(Props[LifecycleActor], "parent")

  parent ! StartChild
  parent ! PoisonPill

  object Fail

  object FailChild

  object Check
  object CheckChild

  class Parent extends Actor {
    val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild => child! Check
    }
  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("Supervided child started")

    override def postStop(): Unit = log.info("Supervides child stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervised actor restarting becase of ${reason.getMessage}")

    override def postRestart(reason: Throwable): Unit =
      log.info("supervised child restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("Child will Fail now")
        throw new RuntimeException("I Failed")

      case Check => log.info("Alive and kicking!")
    }
  }

  val supervisor = system.actorOf(Props[Parent], "supervisor")
  supervisor ! FailChild
  supervisor ! CheckChild
}
