package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsIntro extends App {

  //Step 1. Create an ActorSystem
  val actorSystem: ActorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  //Step 2. Create Actors
  //1. Actors are uniquely identified within an actor system
  //2. Messages are passed and processed asynchronously
  //3. Each actor has a unique behaviour or unique way of processing the messages
  //4. Actor are (really) encapsulated
  //5. Actors can have constructor arguments
  class WorldCountActor extends Actor {
    var totalWords = 0

    override def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[Word Counter] I received $message")
        totalWords += message.split(" ").length
      case msg => println(s"[Word Counter] I cannot understand ${msg.toString}")
    }
  }

  //Step 3. Instantiate our Actor
  val wordCounter: ActorRef = actorSystem.actorOf(Props[WorldCountActor], "wordCounter")

  //Step4. Communicate with the Actor!
  wordCounter ! "Hello World Akka Actors!"


  object Person {
    def props(name: String) = Props(new Person(name))
  }
  class Person (name: String) extends Actor {
    override def receive: Receive = {
      case "Hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  val bob: ActorRef = actorSystem.actorOf(Person.props("Bob")) //Using a constructor argument

  bob ! "Hi"

}
