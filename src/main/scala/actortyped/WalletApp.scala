package actortyped

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

object Wallet {
  def apply(): Behavior[Int] = {
    Behaviors.receive{
      (context, message) => {
        context.log.info(s"Received $message")
        Behaviors.same
      }
    }
  }
}


object WalletApp extends App {

  val guardian: ActorSystem[Int] = ActorSystem(Wallet(), "wallet")

  guardian ! 1
  guardian ! 10

  println("Press ENTER to terminate")
  scala.io.StdIn.readLine()
  guardian.terminate()
}
