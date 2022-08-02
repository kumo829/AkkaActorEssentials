package infra

import actors.ActorsIntro.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, PoisonPill, Props, Timers}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object TimersSchedulers extends App {
  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("SchedulersTimersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduling reminder for simpleActor")


  import system.dispatcher

  //  system.scheduler.scheduleOnce(1 second) {
  //    simpleActor ! "reminder"
  //  }
  //
  //  var routine: Cancellable = system.scheduler.scheduleAtFixedRate(1 second, 2 seconds)(() => {
  //    simpleActor ! "heartbeat"
  //  })
  //
  //  system.scheduler.scheduleOnce(5 seconds) {
  //    routine.cancel()
  //  }


  class SelfClosingActor extends Actor with ActorLogging {

    var schedule = createTimeoutWindow()

    def createTimeoutWindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }
    }

    override def receive: Receive = {
      case "timeout" =>
        log.info("Stopping myself")
        context.stop(self)
      case message =>
        log.info(s"Received $message")
        schedule.cancel()
        schedule = createTimeoutWindow()
    }
  }

//  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosing")
//  system.scheduler.scheduleOnce(250 millis) {
//    selfClosingActor ! "ping"
//  }
//
//  system.scheduler.scheduleOnce(2 seconds) {
//    system.log.info("seding pong to the self-closing actor")
//    selfClosingActor ! "pong"
//  }


  //Timer

  case object TimerKey

  case object Start

  case object Reminder

  case object Stop

  class TimerBasedSelfClosingActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startTimerWithFixedDelay(TimerKey, Reminder, 1 second)

      case Reminder =>
        log.info("I am alive")

      case Stop =>
        log.warning("Stopping")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val timerActor = system.actorOf(Props[TimerBasedSelfClosingActor], "timerActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timerActor ! Stop
  }

}
