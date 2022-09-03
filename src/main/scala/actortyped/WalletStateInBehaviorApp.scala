package actortyped

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object WalletStateInBehaviorApp extends App {
  sealed trait Command
  final case class Increase(amount: Int) extends Command
  final case object Deactivate extends Command
  final case object Activate extends Command

  def apply(): Behavior[Command] = activated(0)

  def activated(count: Int): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Increase(amount) =>
          val current = count + amount
          context.log.info(s"Increasing to $current")
          activated(current)
        case Deactivate => deactivated(count)
        case Activate => Behaviors.same
      }
    }

  def deactivated(count: Int): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Increase(amount) =>
          context.log.info("Wallet is deactivated. Can't connect")
          Behaviors.same
        case Deactivate => Behaviors.same
        case Activate =>
          context.log.info("activating")
          activated(count)
      }
    }



  val guardian: ActorSystem[Command] = ActorSystem(WalletStateInBehaviorApp(), "wallet-on-off")
  guardian ! Increase(1)
  guardian ! Deactivate
  guardian ! Increase(1)
  guardian ! Activate
  guardian ! Increase(1)
}
