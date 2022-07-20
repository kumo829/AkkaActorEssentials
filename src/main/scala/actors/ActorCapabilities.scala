package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App{
  class SimpleActor extends Actor {

//    context.system //A reference to the ActorSystem
//    context.self or simply self //A reference to the same actor, equivalent to `this`

    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello, there!" //replying to a message
      case message: String => println(s"[simple actor] I have received '$message'")
      case number: Int => println(s"[simple actor] I have received a number $number")
      case SpecialMessage(content) => println(s"[simple actor] I have received something special $content")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref: ActorRef) => ref ! "Hi!"
    }
  }

  val actorSystem = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "Hello, Actor"

  //1. Messages can be of any type
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE
  // in practice use case classes and case objects
  simpleActor ! 42

  case class SpecialMessage(content: String)
  simpleActor ! SpecialMessage("some special content")

  //2. Actors have information about their context and about themselves

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  //3 - Actors can reply to messages using their context
  val alice = actorSystem.actorOf(Props[SimpleActor], "alice")
  val bob = actorSystem.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)
}
