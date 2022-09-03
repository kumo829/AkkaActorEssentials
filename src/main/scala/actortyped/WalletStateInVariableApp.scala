package actortyped

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}


object WalletState {
  sealed trait Command
  final case class Increase(dollars: Int) extends Command
  final case class Decrease(dollars: Int) extends Command



  def apply(total: Int, max: Int): Behavior[Command] = {
    Behaviors.receive { (context, message) =>
      message match {
        case Increase(dollars) =>
          val current = total + dollars
          if (current <= max) {
            context.log.info(s"Increasing to $current")
            apply(current, max)
          } else {
            context.log.info(s"I'm overloaded. Counting '$current' while max is '$max'. Stopping")
            Behaviors.stopped
          }
      }
    }
  }

}

object WalletStateInVariableApp extends App {
  val guardian: ActorSystem[WalletState.Command] = ActorSystem(WalletState(0, 2), "wallet-state")

  guardian ! WalletState.Increase(1)
  guardian ! WalletState.Increase(1)
  guardian ! WalletState.Increase(1)

}
