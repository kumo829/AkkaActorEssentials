package infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, Broadcast, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory

object Routers extends App {

  /*
    Method 1. Manual router
   */
  class Master extends Actor {

    //step 1. Create routees
    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_$i")
      context.watch(slave)

      ActorRefRoutee(slave)
    }

    //step 2. Define router
    private var router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {

      //step 4. handle the termination/lifecycle of the routees
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router = router.addRoutee(newSlave)

      //step 3. Route the messages
      case message => router.route(message, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RoutersDemo", ConfigFactory.load().getConfig("routersDemo"))
  val master = system.actorOf(Props[Master])

  //  for(i <- 1 to 10)
  //    master ! s"[${i}]Hello to the world"


  /*
  Method 2. A Router actor with its own children (pool router)
   */

  // 2.1. Programmatically (in code)
  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")

//  for (i <- 1 to 10)
//    poolMaster ! s"[${i}]Hello to the world"

  // 2.2. From configuration
  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")
//  for (i <- 1 to 10)
//    poolMaster2 ! s"[${i}]Hello to the world"

  /*
  Method 3. Router with Actors created elsewhere (Group router)
   */

  // 3.1. In code.
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"slave_${i}")).toList

  val slavePaths = slaveList.map(slaveRef => slaveRef.path.toString)

  val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())

//  for (i <- 1 to 10)
//    groupMaster ! s"[${i}]Hello to the world"

  // 3.2. From Configuratiion
  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMaster2")
//  for (i <- 1 to 10)
//    groupMaster ! s"[${i}]Hello to the world"


  /*
  Speciall messages: Messages, PoisonPill, Kill, AddRoutee, RemoveRoutee, GetRoutee
   */

  groupMaster2 ! Broadcast("Hello, everyone")
}
