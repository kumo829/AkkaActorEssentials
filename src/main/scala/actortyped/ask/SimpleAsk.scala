package actortyped.ask

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout

import scala.concurrent.duration.{DurationInt, SECONDS}
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

object Guardian {
  sealed trait Command
    case class Start(texts: List[String]) extends Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    val manager: ActorRef[Manager.Command] = context.spawn(Manager(), "manager")

    Behaviors.receiveMessage {

      case Start(texts) =>
        manager ! Manager.Delegate(texts)
        Behaviors.same
    }
  }
}

private object Manager {
  sealed trait Command
  final case class Delegate(texts: List[String]) extends Command
  private final case class Report(description: String) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      implicit val timeout: Timeout = Timeout(3 seconds)

      Behaviors.receiveMessage {
        case Delegate(texts) =>
          texts.foreach { text =>
            val worker: ActorRef[Worker.Command] = context.spawn(Worker(text), s"Worker-$text")

            context.ask(worker, Worker.Parse) {
              case Success(Worker.Done) => Report(s"$text parsed by ${worker.path.name}")
              case Failure(exception)   => Report(s"parsing $text has failing with error: ${exception.getLocalizedMessage}")
            }
          }

          Behaviors.same

        case Report(description) =>
          context.log.info(description)
          Behaviors.same
      }

    }
}

private object Worker {
  sealed trait Command
  case class Parse(replyTo: ActorRef[Worker.Reponse]) extends Command

  sealed trait Reponse
  case object Done extends Reponse

  def apply(text: String): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Parse(replyTo) =>
          fakeParsing(text)
          prettyPrint(context, "done")
          replyTo ! Worker.Done
      }
      Behaviors.stopped
    }

  def fakeParsing(file: String): Unit = {
    val endTime = System.currentTimeMillis + Random.between(2000, 4000)
    while (endTime > System.currentTimeMillis) {}
  }

  def prettyPrint(context: ActorContext[_], message: String): Unit = {
    context.log.info(s"${context.self.path.name}: $message")
  }
}

object SimpleAsk extends App {
  val system: ActorSystem[Guardian.Command] = ActorSystem(Guardian(), "example-ask-without-content")
  system ! Guardian.Start(List("text-a", "text-b", "text-c"))
}
