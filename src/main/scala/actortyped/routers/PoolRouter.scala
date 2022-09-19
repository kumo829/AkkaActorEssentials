package actortyped.routers

import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, PoolRouter, Routers}

object PoolRouter extends App {

  private object Worker {
    sealed trait Command
      case class DoLog(text: String) extends Command
      case class DoBroadcastLog(text: String) extends Command

    def apply(): Behavior[Command] =
      Behaviors.setup { context =>
        context.log.info("Starting worker")

        Behaviors.receiveMessage {
          case DoLog(text) =>
            context.log.info("Got message: {}", text)
            Behaviors.same

          case DoBroadcastLog(text) =>
            context.log.info("Got broadcast message: {}", text)
            Behaviors.same
        }
      }
  }

  private object Manager {
    def apply(): Behavior[Unit] =

      Behaviors.setup[Unit] { context =>

        val behavior: Behavior[Worker.Command] = Behaviors.supervise(Worker()).onFailure[Exception](SupervisorStrategy.restart)
        val pool: PoolRouter[Worker.Command] = Routers.pool(poolSize = 4)(behavior).withRandomRouting() //default is Round Robin

        val router = context.spawn(pool, "worker-pool")

        (0 to 10).foreach { n =>
          router ! Worker.DoLog(s"message: ${n}")
        }

        Behaviors.empty
      }

  }

  private object BroadCastManager {
    def apply(): Behavior[Unit] =
      Behaviors.setup[Unit] { context =>

        val behavior: Behavior[Worker.Command] = Behaviors.supervise(Worker()).onFailure[Exception](SupervisorStrategy.restart)
        val routingBehavior: PoolRouter[Worker.Command] = Routers.pool(poolSize = 4)(behavior).withBroadcastPredicate(_.isInstanceOf[Worker.DoBroadcastLog])

        val routerWithBroadcast = context.spawn(routingBehavior, "pool-with-broadcast")

        routerWithBroadcast ! Worker.DoBroadcastLog("This is a broadcast message")

        Behaviors.empty
      }
  }

  val actorSystem = ActorSystem(Manager(), "actorSystem")

  println("Press ENTER to send a broadcast")
  scala.io.StdIn.readLine()

  actorSystem.terminate()

  val broadcastActorSystem = ActorSystem(BroadCastManager(), "broadcasSystem")
  println("Press ENTER to terminate")
  scala.io.StdIn.readLine()

  broadcastActorSystem.terminate()
}
