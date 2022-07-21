package actors

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {

    //    context.system //A reference to the ActorSystem
    //    context.sender or sender //A reference to the actor that last sent a message Actor (or Actor.noSender)
    //    context.self or simply self //A reference to the same actor, equivalent to `this`

    override def receive: Receive = {
      case "Hi!" => sender ! "Hello, there!" //replying to a message
      case message: String => println(s"[simple actor] I have received '$message'")
      case number: Int => println(s"[simple actor] I have received a number $number")
      case SpecialMessage(content) => println(s"[simple actor] I have received something special $content")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref: ActorRef) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward content + "-s"
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

  //3. Actors can reply to messages using their context
  val alice = actorSystem.actorOf(Props[SimpleActor], "alice")
  val bob = actorSystem.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  //4. Dead Letters
  //  alice ! "Hi!"

  //5. Forwarding messages
  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi!", bob)

  /**
   * Exercises:
   * 1. Create a Counter Actor that holds an internal variable and will respond to:
   * - Increment
   * - Decrement
   * - Print
   *
   * 2. Create a Bank Account as an Actor, which receives messages to
   * - Deposit an amount
   * - Withdraw an amount
   * - Statement
   *
   * The actor will react to Deposit and Withdraw by sending back or replying with a Success/Failure of each of those operations
   * This Actor will interact with some other kind of Actor
   */


  //Domain of the counter
  object CounterActor {
    case object Increment

    case object Decrement

    case object Print
  }

  class CounterActor extends Actor {

    import CounterActor._

    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[Counter] The counter value is $count")
    }
  }


  val counterActor = actorSystem.actorOf(Props[CounterActor], "counterActor")
  (1 to 3).foreach(_ => counterActor ! CounterActor.Increment)
  counterActor ! CounterActor.Print
  counterActor ! CounterActor.Decrement
  counterActor ! CounterActor.Print

  //Domain of BankAccount
  object BankAccountActor {
    def props(initialBalance: Int) = Props(new BankAccountActor(initialBalance))

    case class Deposit(amount: Int, ref: ActorRef)
    case class Withdraw(amount: Int, ref: ActorRef)
    case object Statement
  }

  class BankAccountActor(var initialBalance: Int) extends Actor {

    import BankAccountActor._

    override def receive: Receive = {
      case Deposit(amount, ref) =>
        println("Making a deposit")
        initialBalance += amount
        ref ! Success
      case Withdraw(amount, ref) =>
        println("Making a withdraw")
        initialBalance -= amount
        ref ! Success
      case Statement =>
        println(s"The current balance is $initialBalance")
    }
  }

  class BankTeller extends Actor {
    override def receive: Receive = {
      case Success => println("The operation was completed successfully")
      case Failure => println("The operation couldn't be performed")
    }
  }

  case class Success()

  case class Failure()

  val bankAccount = actorSystem.actorOf(BankAccountActor.props(1000), "bankAccount")
  val bankTeller = actorSystem.actorOf(Props[BankTeller], "bankTeller")

  bankAccount ! Deposit(500, bankTeller)
  bankAccount ! Deposit(500, bankTeller)
  bankAccount ! Withdraw(300, bankTeller)
  bankAccount ! Statement

}
