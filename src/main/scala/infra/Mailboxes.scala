package infra

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App {
  val system = ActorSystem("MailboxDemo", ConfigFactory.load().getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /*
  Case 1. Custom priority mailbox
  P0 -> Most important
  P1
  P2
  P3
   */

  // Step 1. Mailbox definition
  class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    PriorityGenerator {
      case message: String if message.startsWith("[P0]") => 0
      case message: String if message.startsWith("[P1]") => 1
      case message: String if message.startsWith("[P2]") => 2
      case message: String if message.startsWith("[P3]") => 3
      case _ => 4
    }
  )

  // Step 2. Make it known in the config
  // Step 3. Attach the dispatcher to an Actor

  val supportTicketLogger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  //  supportTicketLogger ! PoisonPill
  //  supportTicketLogger ! "[P3] this would be nice to have"
  //  supportTicketLogger ! "[P1] do this when you have time"
  //  supportTicketLogger ! "[P0] this must be delivered FIRST!"


  /*
  Case 2. Controll-aware mailbox
  We use the UnboundedControlAwareMailbox
  */
  // Step 1. Mark important messages as control messages
  case object ManagementTicket extends ControlMessage

  // Method 1
  // Step 2. Configure who gets the mailbox (make the actor attach to the mailbox)
  val controlAwareActor = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))
  controlAwareActor ! "[P3] this would be nice to have"
  controlAwareActor ! "[P1] do this when you have time"
  controlAwareActor ! "[P0] this must be delivered FIRST!"
  controlAwareActor ! ManagementTicket


  // Method 2 Using deployment config
  val altControlAwareActor = system.actorOf(Props[SimpleActor], "altControlAwareActor")
  altControlAwareActor ! "[P3] this would be nice to have"
  altControlAwareActor ! "[P1] do this when you have time"
  altControlAwareActor ! "[P0] this must be delivered FIRST!"
  altControlAwareActor ! ManagementTicket
}

