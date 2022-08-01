package actors

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{BackoffOpts, BackoffSupervisor}

import java.io.File
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.language.postfixOps

object BackoffSupervisorPattern extends App {

  case object ReadFile

  class FileBasedPersistentActor extends Actor with ActorLogging {
    var datasource: Source = null

    override def preStart(): Unit = log.info("Persistent actor starting")

    override def postStop(): Unit = log.info("Persistent actor has stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info("Persistent actor restarting...")

    override def receive: Receive = {
      case ReadFile =>
        if (datasource == null)
          datasource = Source.fromFile(new File("src/main/testfiles/important.txt"))

        log.info("I've read some important data: {}", datasource.getLines().toList)
    }
  }

  val system = ActorSystem("BackoffSupervisorDemo")
  val simpleActor = system.actorOf(Props[FileBasedPersistentActor], "simpleActor")
  //  simpleActor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onFailure(
      Props[FileBasedPersistentActor],
      "simpleBackoffActor",
      3 seconds,
      30 seconds,
      0.2)
  )

  val simpleBackoffSupervisor = system.actorOf(simpleSupervisorProps, "simpleSupervisor")
//  simpleBackoffSupervisor ! ReadFile

  /*
   * simpleSupervisor
   * - child called simpleBackoffActor (props of type FileBasedPersistendActor)
   * - supervisor strategy is the default one (restarting on everything)
   *  - first attempt after 3 seconds
   *  - next attempt is 2x the previous attempt
   */

  val stopSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[FileBasedPersistentActor],
      "stopBackoffActor",
      3 seconds,
      30 seconds,
      0.2).withSupervisorStrategy(
      OneForOneStrategy() {
        case _ => Stop
      }
    )
  )

  val stopSupervisor = system.actorOf(stopSupervisorProps, "stopSupervisor")
//    stopSupervisor ! ReadFile


  class EagerFileBasedPersistentActor extends FileBasedPersistentActor {
    override def preStart(): Unit = {
      log.info("Eager Actor starting")
      datasource = Source.fromFile(new File("src/main/testfiles/important.txt"))
    }
  }

  val eagerActor = system.actorOf(Props[EagerFileBasedPersistentActor]) //ActorInitializationException => Stop

  val repeatedSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[EagerFileBasedPersistentActor],
      "eagerActor",
      1 second,
      30 seconds,
      0.1
    )
  )

  val repeatedSupervisor = system.actorOf(repeatedSupervisorProps, "eagerSupervisor")

  /*
  eagerSupervisor
    - child eagerActor
      - will die on start with ActorInitializationException
      - trigger the supervisor strategy in eagerSupervisor => Stop EagerActor
    - backoff will kick in after 1, 2, 4, 8, 16 seconds
   */
}
