package actortyped

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object Guardian {
  sealed trait Command
    case class Start(texts: List[String]) extends Command

  def apply(): Behavior[Command] = {
    // This factory creates a behavior that is executed only once - when the actor is instantiated. It creates a behavior
    // from a function with only one input parameter, the context
    Behaviors.setup {
      context =>
        context.log.info("Setting up.")

        val manager: ActorRef[Manager.Command] = context.spawn(Manager(), "manager")

        Behaviors.receiveMessage {
          case Start(texts) =>
            manager ! Manager.Delegate(texts)
            Behaviors.same
        }
    }
  }
}

private object Manager {
  sealed trait Command
    final case class Delegate(texts: List[String]) extends Command
    private case class WorkerDoneAdapter(response: Worker.Response) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup{ context =>
        val adapter: ActorRef[Worker.Response] = context.messageAdapter(rsp => WorkerDoneAdapter(rsp))

      Behaviors.receiveMessage{ message =>
        message match {
          case Delegate(texts) =>
            texts.map { text =>
              val worker: ActorRef[Worker.Command] =
                context.spawn(Worker(), s"worker-$text")
                context.log.info(s"Sending task $text to $worker")
                worker ! Worker.Parse(adapter, text)
            }

            Behaviors.same

          case WorkerDoneAdapter(Worker.Done(text)) =>
            context.log.info(s"Parsed text $text")
            Behaviors.same
        }
      }

    }

}

private object Worker {
  sealed trait Command
    final case class Parse(replyTo: ActorRef[Worker.Response], text: String) extends Command

  sealed trait Response
    final case class Done(text: String) extends Response

  def apply(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Parse(replyTo, text) =>
          val parsed = naiveParsing(text)
          context.log.info(
            s"'${context.self}' DONE!. Parsed result: $parsed")
          replyTo ! Worker.Done(parsed)
          Behaviors.stopped
      }
    }

  def naiveParsing(text: String): String =
    text.replaceAll("-", "")
}

object Hierachy extends App {
  val guardian :ActorSystem[Guardian.Command] = ActorSystem(Guardian(), "ac")

  guardian ! Guardian.Start(List("-one-", "--two--"))

  println("press ENTER to terminate")
  scala.io.StdIn.readLine()
  guardian.terminate()
}
