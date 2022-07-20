# Akka Actors Essentials

Akka Actors is a new wat to think about concurrent, distributed, and fault-tolerant code.

## Why Akka:

1. it speeds up the development of concurrent applications by as much as 10x
2. its demand has exploded
3. it's a highly marketable skill
4. it's incredibly fun - once tasted Akka, you'll never want to get back to threads

## Problems with the current thread model
**#1 OOP encapsulation is only valid in the Single Thread Model**. The OOP encapsulation is broken in a multithreaded environment, 
so we have to use locks, which introduce different problems like deadlocks, livelocks, etc. 
And this model does not scale well when there are complex structures in the application, 


**#2 Delegating something to a Thread is a pain**. Sending a signal to a ready running thread.

**#3 Tracing and dealing with errors is complex**. Most of the time, the errors related with multithreading are dark and too general.


With traditional objects
- Each instance has its own state as data (attributes inside the class).
- Interacts with other objects via methods.

With Actors
- Also store its state as data.
- The interaction with the world is different, by sending messages to them, asynchronously.

So, in a sense **Actores are objects that we can't access directly, but only send messages to**.


## Messages and Behaviour
We follow four steps to create and send messages to an Actor:

**Step 1. Create an ActorSystem**

```scala
val actorSystem: ActorSystem = ActorSystem("firstActorSystem")
println(actorSystem.name)
```

**Step 2. Create Actors**
```scala
class WorldCountActor extends Actor {
var totalWords = 0

    override def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[Word Counter] I received $message")
        totalWords += message.split(" ").length
      case msg => println(s"[Word Counter] I cannot understand ${msg.toString}")
    }
}
```

**Step 3. Instantiate our Actor**
```scala
val wordCounter: ActorRef = actorSystem.actorOf(Props[WorldCountActor], "wordCounter")
```

**Step4. Communicate with the Actor!**
```scala
wordCounter ! "Hello World Akka Actors!"
```

## Examples
- [Intro to actors and basic demo](./src/main/scala/actors/ActorsIntro.scala)

## References
- [Akka Essentials with Scala | Rock the JVM in Udemy](https://www.udemy.com/course/akka-essentials/learn/lecture/12418624#overview)