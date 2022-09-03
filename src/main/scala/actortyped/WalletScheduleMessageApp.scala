package actortyped

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


object WalletTimer {
  /**
   * Making the protocol private or public is a common pattern used in Akka. This allows you to provide both a public
   * API, which is a set of messages that other actors can use, and a private API, which includes the actor's internal
   * functionality.
   */
  sealed trait Command
  final case class Increase(dollars: Int) extends Command
  final case class Deactivate(seconds: Int) extends Command
  private final case object Activate extends Command //This ensure that the wallet can only be reactivated by the actor itself

  def apply(): Behavior[Command] = activated(0)

  def activated(total: Int): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      Behaviors.withTimers { timers =>
        message match {
          case Increase(dollars) =>
            val current = total + dollars
            context.log.info(s"Increasing to $current")
            activated(current)
          case Deactivate(time) =>
            timers.startSingleTimer(Activate, time seconds)
            deactivated(total)
          case Activate =>
            Behaviors.same
        }
      }
    }

  def deactivated(total: Int): Behavior[Command] =
    Behaviors.receive{ (context, message) =>
      message match {
        case Increase(amount) =>
          context.log.info("Wallet is deactivated. Can't increase")
          Behaviors.same
        case Deactivate(seconds) =>
          context.log.info("Wallter is already deactivated")
          Behaviors.same
        case Activate =>
          context.log.info(s"Activating with $total...")
          activated(total)
      }
    }
}

object WalletScheduleMessageApp extends App {
  val guardian: ActorSystem[WalletTimer.Command] = ActorSystem(WalletTimer(), "wallet-timer")
  guardian ! WalletTimer.Increase(2)
  guardian ! WalletTimer.Deactivate(3)
  guardian ! WalletTimer.Increase(2)

  println("Press ENTER to terminate")
  scala.io.StdIn.readLine()
  guardian ! WalletTimer.Increase(2)
  guardian.terminate()
}
