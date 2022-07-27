package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info("The message is: {}", message)
    }
  }

  /**
   * 1. Inline configuration
   */
  val configString =
    """
      | akka {
      |   loglevel = "INFO"
      | }
    """.stripMargin

  val config = ConfigFactory.parseString(configString)
  val actorSystem = ActorSystem("actorSystem", ConfigFactory.load(config))

  val actor = actorSystem.actorOf(Props[SimpleLoggingActor], "simpleActor")

  actor ! "A message"


  /**
   * 2. Using the default namespace in the default config file application.conf
   */
  val defaultConfigFileSystem = ActorSystem("defaultConfigDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor], "simpleDefaultConfigActor")

  defaultConfigActor ! "Hello :)"


  /**
   * 3. Separated namespace in the default config file
   */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialAkkaConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor], "specialConfigActor")

  specialConfigActor ! "Hello, I'm special :)"

  /**
   * 4. Non-default config file
   */
  val nonDefaultConfig = ConfigFactory.load("secretFolder/secretConfig.conf")
  println(s"Separeted config log level ${nonDefaultConfig.getString("akka.loglevel")}")
  val nonDefaultConfigSystem = ActorSystem("NonDefaultSystem", nonDefaultConfig)
  val nonDefaultConfigActor = nonDefaultConfigSystem.actorOf(Props[SimpleLoggingActor], "nonDefaultConfigActor")

  nonDefaultConfigActor ! "Hello, I'm also special :)"

  /**
   * Using different format like Json or Properties
   */
  val jsonConfig = ConfigFactory.load("secretFolder/jsonConfig.json")
  val jsonSystem = ActorSystem("Jsonsystem", jsonConfig)
  val jsonActor = jsonSystem.actorOf(Props[SimpleLoggingActor], "jsonActor")

  jsonActor ! "This is json!"

}
