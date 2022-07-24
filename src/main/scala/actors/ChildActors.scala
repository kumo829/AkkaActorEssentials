package actors

import actors.ChildActors.Parent.{CreateChild, TellChild}
import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}

object ChildActors extends App{
  //Actors can create other Actors using the context

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {
    import Parent._
    override def receive: Receive = {
      case CreateChild(name) =>
      println(s"${self.path} creating child")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))

    }

    def withChild(ref: ActorRef): Receive = {
      case TellChild(message) =>
        if(ref != null) ref forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got $message")
    }
  }

  val actorSystem = ActorSystem("actorSystem")
  val parent = actorSystem.actorOf(Props[Parent], "parent")

  parent ! CreateChild("child")
  parent ! TellChild("Hey, kid!")

  val childSelection: ActorSelection = actorSystem.actorSelection("/user/parent/child")
  childSelection ! "I found you!"
}
