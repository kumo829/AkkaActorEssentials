package actors

import actors.ChangingActorBehaviour.FussyKid.{KidAccept, KidReject}
import actors.ChangingActorBehaviour.Mom.MomStart
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehaviour extends App {

  object Mom {
    case class Food(foodName: String)

    case class Ask(question: String)

    case class MomStart(kidRef: ActorRef)

    val VEGETABLES = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor() {

    import Mom._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLES)
        kidRef ! Food(VEGETABLES)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?")
      case KidAccept => println("Yay, my kid is happy!")
      case KidReject => println("My kid is sad but healthy")
    }
  }

  object FussyKid {
    case object KidAccept

    case object KidReject
  }

  class FussyKid extends Actor {

    import Mom._
    import FussyKid._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLES) => context.become(sadReceive, false) //change the receive handler to sadReceive
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLES) => context.unbecome()
      case Food(CHOCOLATE) => context.become(happyReceive, false) //change the receive handler to sadReceive
      case Ask(_) => sender() ! KidReject
    }
  }

  val actorSystem = ActorSystem("actorSystem")

  val kid = actorSystem.actorOf(Props[FussyKid])
  val mom = actorSystem.actorOf(Props[Mom])

  mom ! MomStart(kid)


  /**
   * Exercise:
   * 1. Create the Counter Actor with context.became and NO MUTABLE STATE
   */

  object Counter {
    case object Increment

    case object Decrement

    case object Print
  }

  class Counter extends Actor {

    import Counter._

      override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment => context.become(countReceive(currentCount + 1))
      case Decrement => context.become(countReceive(currentCount - 1))
      case Print => println(s"[Counter] Current count = $currentCount")
    }
  }

  val counter = actorSystem.actorOf(Props[Counter], "counterActor")

  (1 to 5).foreach(_ => counter ! Counter.Increment)
  (1 to 3).foreach(_ => counter ! Counter.Decrement)
  counter ! Counter.Print

  /**
   * Exercise:
   * 2. A simplified voting system
   */

  case class Vote(candidate: String)

  case object VoteStatusRequest

  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(candidate) => context.become(voted(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Option(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {

    override def receive: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(_ ! VoteStatusRequest)
        context.become(awaitResponses(citizens, Map()))
    }

    def awaitResponses(citizens: Set[ActorRef], currentVotes: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = citizens - sender()
        val currentVotesOfCandidate = currentVotes.getOrElse(candidate, 0)
        val newVotes = currentVotes + (candidate -> (currentVotesOfCandidate + 1))

        if(newStillWaiting.isEmpty) {
          println(s"[Aggregator] votes: $newVotes")
        } else {
          context.become(awaitResponses(newStillWaiting, newVotes))
        }
    }

  }

  val alice = actorSystem.actorOf(Props[Citizen])
  val bob = actorSystem.actorOf(Props[Citizen])
  val charlie = actorSystem.actorOf(Props[Citizen])
  val daniel = actorSystem.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = actorSystem.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))
  //Print the status of the votes

}
