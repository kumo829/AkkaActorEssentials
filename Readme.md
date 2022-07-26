# Akka Actors Essentials

| Technology                                                                                                                            | Version |
|---------------------------------------------------------------------------------------------------------------------------------------|:-------:|
| <img align="left" alt="Scala" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/scala/scala-original.svg" />   Scala | 2.13.8  |
| <img align="left" alt="Akka" width="40" src="https://www.svgrepo.com/show/353381/akka.svg" /> Akka Actors                             | 2.6.19  |
| <img align="left" alt="sbt" width="40" src="https://upload.wikimedia.org/wikipedia/commons/4/43/Sbt-logo.svg" /> SBT                  | 1.7.1  |

Akka Actors is a new wat to think about concurrent, distributed, and fault-tolerant code.

Akka is an open source library available on Apache License Version 2.0.

We can develop applications using Akka to utilize today's multicore CPUs efficiently to improve application performance. In the old versions, Scala had the Actors library; Lightbend has removed it from Scala and moved it to the Akka Toolkit. Now the scala.actors library is no longer useful, and it is deprecated.

Akka is an Event-Driven middleware framework for building high-performance, highly Scalable, highly available, and Reliable distributed applications in Java and Scala.

## Why Akka:


Akka is an open source library or a toolkit and runtime from Lightbend Inc. (formerly known as Typesafe), written in the Scala language to develop these attributes:

- Concurrent
- Distributed
- Fault-Tolerant
- Message-Driven
- Responsive
- Highly Scalable
- Asynchronous
- Non-blocking
- Highly performant
- Highly available
- Ease of maintenance

And provides the following benefits:

1. it speeds up the development of concurrent applications by as much as 10x
2. its demand has exploded
3. it's a highly marketable skill
4. it's incredibly fun - once tasted Akka, you'll never want to get back to threads
5. It is open source  
6. By design, it is distributed, and it's very easy to use distributed applications  
7. It supports clustering 
8. It supports Reactive Streams using the Akka Streams module 
9. It is easy to develop highly performant, highly scalable, highly maintainable, and highly available applications using Akka 
10. It supports concurrency using the Actor Model. Unlike Java's Shared-State Model (which is low-level API), the Actor-based Concurrency Model is a high-level API to write Concurrency and Parallelism programming without threads, locking, and other issues. 
11. It supports scalability in both directions:
- Scale up (vertically)
- Scale out (horizontally)
12. It is easy to test Akka application's components
13. By design, it supports Reactive Streams specifications—Message-Driven, Resilient (Fault-Tolerant), Elastic (Scalable), and Responsive
14. It supports self-healing
15. It supports Location Transparency, which means looking-up or identifying the Actors is the same for both local Actors (which are located in the same JVM) or remote Actors (which are located in the remote JVM)

The Akka Toolkit supports the development of concurrent, asynchronous, non-blocking, Resilient, Elastic, Event-Driven, and Responsive systems very easily.

Akka Toolkit supports Message-Driven programming using the Actor Model and without using any message brokers (such as RabbitMQ, Apache ActiveMQ, Apache Kafka, and more).

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

## Features of Akka

The Akka Toolkit supports the following features:

- Actor Model
- Distributed
- Concurrency and Parallelism
- Message-Driven
- Fault-Tolerant
- Highly Scalable
- Akka uses the Typesafe Config Library to support configuration
- Reactive
- Location Transparency
- Remote Actors
- Automatic load balancing
- Self-healing
- Clustering
- Developing WebServers using Akka HTTP
- Cluster sharding
- Remoting

## Building blocks of the Akka Toolkit

- ActorSystem 
- Actor 
- ActorContext 
- ExecutionContext

## Actor Basics
Every `Actor` type derives from:

```scala
trait Actor {
  def receive: Receive //Message handler object, is retreived by Akka, and is invoked when the actor processes a message
}
```

The `Receive` type is an alias:
```scala
type Receive = PartialFunction[Any, Unit]
```
Actors need infrastructure:
```scala
val system: ActorSystem = ActorSystem("actorSystemName")
```

Creating Actors is not done in the traditional way:
```scala
val actor: ActorRef = system.actorOf(Props[MyActor], "myActorName")
```

The only way to communicate with an Actor is by sending a message using the *tell* (`!`) method with the message that you want to send.
The messages can be of any type, including primitive types and custom types as long as they are **immutable** and **serializable**.
```scala
actor ! "hello, Actor"
```

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

## Actor Logging

Actors use logback as default logging implementation, as is asynchronous (because is done with Actors!). There are two types of loggers that can be used in Actors: **explicit** and **implicit** or actor logging.

Explicit logging: 
```scala
class SimpleActor extends Actor {
  val logger = Logging(context.system, this) //actor system, logging source
  
  override def receive: Reveive = {
    case message = logger.info("I received: {}", message.toString)
  }
}
```

Actor Logging (using the trait `ActorLogging`:

```scala
class SimpleActor extends Actor with ActorLogging {
  override def receive: Reveive = {
    case message = log.info("I received: {}", message.toString)
  }
}
```

## Configuration

There are many ways to configure an Akka application. The most common ones are: 
1. Inline configuration. We can use the triple quotes and `stripMargin` to improve readability.
2. Using the default config file `application.conf`.
3. Using a different namespace in the default config file (`application.conf`). 
4. Using a no-default file.
5. Using a configuration file in a different format like JSON or properties.

### Inline configuration

```scala
val configString =
    """
      | akka {
      |   loglevel = "INFO"
      | }
    """.stripMargin

  val config = ConfigFactory.parseString(configString)
  val actorSystem = ActorSystem("actorSystem", ConfigFactory.load(config))

  val actor = actorSystem.actorOf(Props[SimpleLoggingActor], "simpleActor")

  actor ! "A message"
```

## Actor Lifecycle

It's necessary to make a distinction between *Actor instances*, *Actor references*, and *Actor paths*. 

- **Actor instance**: Has methods, such as *receive*, and may have internal state.
- **Actor reference or incarnation**: is the one created using *actorOf*; has the mailbox and can receive messages. It contains one actor instance at any one time and a UUID given by the Actor System.
- **Actor Path**: A space in the Actor System which may or may be not occupied by an Actor reference.

Actors can be subject to a number of actions:
- started: create a new ActorRef with a UUID at a given path.
- suspended: the actor ref will enqueue but NOT process more messages.
- resumed: the actor ref will continue processing more messages.
- restarted: the actor is suspended and the actor instance is swapped. Internal state is destroyed on restart. 
- stopped: frees the actor ref within a path.

## The Backoff Supervisor Pattern
This pattern wants to solve a big pain that we often see in Actors: the repeated restart of Actors when there is an error interacting with external resources.

Restarting Actors immediately might do more harm than good is several actors are restarted at the same time (the external resource could go down again or the actors can be blocked).

This pattern introduces exponential delays and randomness in between the attempts to rerun a supervision strategy.

To implement this pattern we use the `BackoffOpts` object provided by Scala:

```scala
  val repeatedSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[EagerFileBasedPersistentActor],
      "eagerActor",
      1 second,
      30 seconds,
      0.1
    )
  )
```

## Routers
Routers allow to delegate/spread work to multiple Actors at the same time.

Routers can use different algorithms to select to which Actor to send the message to:
- Round robin
- Random
- Smallest Mailbox
- Broadcast
- Scatter-gather-first
- Tail-chipping
- Consistent-hashing

There are 3 ways to create Routers

1. By hand using the `akka.routing.Router`.
2. Using a Pool router (a router Actor with its own children). This option offers two different approaches:
   - Programmatically (in code).
   - From configuration.
3. Using a Group Router (with actors created elsewhere)

## Akka Patterns
- [Stashng Messages](./src/main/scala/patterns/StashPattern.scala)
- [Ask Pattern](./src/main/scala/patterns/AskPattern.scala)
- [Finite State Machine](./src/main/scala/patterns/FiniteStateMachinePattern.scala)


## Examples
- [Intro to actors and basic demo](./src/main/scala/actors/ActorsIntro.scala)
- [Actor capabilities](./src/main/scala/actors/ActorCapabilities.scala)
- [Change Actor's behaviour using context.become](./src/main/scala/actors/ChangingActorBehaviour.scala)
- [Child Actors](./src/main/scala/actors/ChildActors.scala) & [Child Actors Exercise](./src/main/scala/actors/ChildActorsExercise.scala)
- [Intro to TestKit](./src/test/scala/BasicSpec.scala)
- [TestProbes](./src/test/scala/TestProbeSpec.scala)
- [Timed Assertions](./src/test/scala/TimedAssertionSpec.scala)
- [Synchronous test](./src/test/scala/SynchronousTestingSpec.scala)
- [Stopping and watching Actors](./src/main/scala/actors/StartingStoppingActors.scala)
- [Actor lifecycle](./src/main/scala/actors/ActorLifecycle.scala)
- [Backoff Supervisor Pattern](./src/main/scala/actors/BackoffSupervisorPattern.scala)
- [Schedulers and Timers](./src/main/scala/infra/TimersSchedulers.scala)
- [Routers](./src/main/scala/infra/Routers.scala)
- [Mailboxes](./src/main/scala/infra/Mailboxes.scala)

## References
- [Akka Essentials with Scala | Rock the JVM in Udemy](https://www.udemy.com/course/akka-essentials/learn/lecture/12418624#overview)
- [Akka Classic Actor reference](https://doc.akka.io/docs/akka/current/index-actors.html)
- [Scala Asynchronous Programming](https://app.pluralsight.com/library/courses/scala-asynchronous-programming/table-of-contents)