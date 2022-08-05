package patterns

import akka.actor.{ActorRef, ActorSystem, FSM, Props}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object FiniteStateMachinePattern extends App {
  // Step 1. Define the states and data of the Actor
  trait VendingState
  case object Idle extends VendingState
  case object Operational extends VendingState
  case object WaitForMoney extends VendingState

  trait VendingData
  case object Uninitialized extends VendingData
  case class Initialized(inventory: Map[String, Int], prices: Map[String, Int]) extends VendingData
  case class WaitForMoneyData(inventory: Map[String, Int], prices: Map[String, Int], product: String, money: Int, requester: ActorRef) extends VendingData


  // Messages
  case class Initialize(inventory: Map[String, Int], prices: Map[String, Int])
  case class VendingError(reason: String)
  case class RequestProduct(product: String)
  case class Instructions(message: String)
  case class ReceiveMoney(money: Int)
  case object ReceiveMoneyTimeout
  case class Deliver(product: String)
  case class GiveBackChange(amount: Int)

  class VendingMachineFSM extends FSM[VendingState, VendingData] {
    /* In FSM we don't have a receive handler because we don't handle messages directly.
    FSM triggers an event which contains a message and data, so we handle states and events
  */
    startWith(Idle, Uninitialized)

    when(Idle) {
      case Event(Initialize(inventory, prices), Uninitialized) =>
        goto(Operational) using Initialized(inventory, prices)

      case _ =>
        sender() ! VendingError("MachineNotInitializedError")
        stay()
    }

    when(Operational) {
      case Event(RequestProduct(product), Initialized(inventory, prices)) =>
        inventory.get(product) match {
          case None | Some(0) =>
            sender() ! VendingError("ProductNotAvailable")
            stay()

          case Some(_) =>
            val price = prices(product)
            sender() ! Instructions(s"Please insert $price dollars")
            goto(WaitForMoney) using WaitForMoneyData(inventory, prices, product, 0, sender())
        }
    }

    when(WaitForMoney, stateTimeout = 1 second) {
      case Event(StateTimeout, WaitForMoneyData(inventory, prices, product, money, requester)) =>
        requester ! VendingError("RequestTimeOut")
        if(money > 0) requester ! GiveBackChange(money)
        goto(Operational) using Initialized(inventory, prices)

      case Event(ReceiveMoney(amount), WaitForMoneyData(inventory, prices, product, money, requester)) =>
        val price = prices(product)
        if(money + amount >= price) {
          requester ! Deliver(product)

          if(money + amount - price > 0) requester ! GiveBackChange(money + amount - price)
          val newStock = inventory(product) - 1
          val newInventory = inventory + (product -> newStock)

          goto(Operational) using Initialized(newInventory, prices)
        } else {
          val remainingMoney = price - money - amount
          requester ! Instructions(s"Please insert $remainingMoney dollars")

          stay() using WaitForMoneyData(inventory, prices, product, money + amount, requester)
        }
    }

    whenUnhandled{
      case Event(_,_) =>
        sender() ! VendingError("CommandNotFound")
        stay()
    }

    onTransition{
      case stateA -> stateB => log.info(s"Transitioning from $stateA to $stateB")
    }

    initialize()
  }

  val system = ActorSystem("FSMSystem")
  val fsmActor = system.actorOf(Props[VendingMachineFSM])

  fsmActor ! Initialize(Map("coke" -> 10), Map("coke" -> 1))
  fsmActor ! RequestProduct("coke")
  fsmActor ! ReceiveMoney(5)
}
