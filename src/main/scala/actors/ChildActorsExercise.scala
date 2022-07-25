package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercise extends App {

  object WordCounterMaster {
    case class Initialize(nChildren: Int)

    case class WordCountTask(taskId: Int, text: String)

    case class WordCountReply(taskId: Int, count: Int)
  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._
    override def receive: Receive = {
      case Initialize(nChildren) =>
        println("[master] Initializing...")
        val childrenRef = for (c <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"worker_$c")
        context.become(processWordCountRequest(childrenRef, 0, 0, Map()))
    }

    def processWordCountRequest(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] I have received: $text - I till send it to child $currentChildIndex")
        val originalSender = sender()
        val childRef = childrenRefs(currentChildIndex)
        childRef ! WordCountTask(currentTaskId, text)
        val nextChildrenIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(processWordCountRequest(childrenRefs, nextChildrenIndex, newTaskId, newRequestMap))

      case WordCountReply(id, count) =>
        println(s"[master] I have received a reply for task id $id with $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(processWordCountRequest(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"[child] ${self.path} I have received task $id with $text")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = actorSystem.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        master ! "This is a text"
        master ! "becase scala rocks"
        master ! "and akka classic actors rocks too"
        master ! "4"
        master ! "5"
      case count: Int =>
        println(s"[test] I have received $count")
    }
  }

  val actorSystem = ActorSystem("actorSystem")

  val test = actorSystem.actorOf(Props[TestActor], "test")

  test ! "go"
}
