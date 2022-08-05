package patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

object AskPattern extends App {
  case class Read(key: String)

  case class Write(key: String, value: String)

  class KVActor extends Actor with ActorLogging {


    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Trying to read the value at the key $key")
        sender() ! kv.get(key) // Option[String]
      case Write(key, value) =>
        log.info(s"Writing the value $value for the key $key")
        context.become(online(kv + (key -> value)))
    }
  }


  case class RegisterUser(username: String, password: String)

  case class Authenticate(username: String, password: String)

  case class AuthFailure(message: String)

  case object AuthSuccess

  object AuthManager {
    val AUTH_FAILURE_NOT_FOUND = "username not found"
    val AUTH_FAILURE_PASSWORD_INCORRECT = "password incorrect"
    val AUTH_FAILURE_SYSTEM = "system error"
  }

  class AuthManager extends Actor with ActorLogging {

    import AuthManager._

    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    protected val authDb = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) => authDb ! Write(username, password)
      case Authenticate(username, password) => handleAuthentication(username, password)
    }

    def handleAuthentication(username: String, password: String) = {
      val originalSender = sender()

      val future = authDb ? Read(username)

      future.onComplete {
        case Success(None) =>
          log.info("Success[None]")
          originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Success(Some(dbPassword)) =>
          log.info("Success(Some)")
          if (dbPassword == password) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
        case Failure(exception) =>
          log.info("Failure")
          originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
      }
    }
  }


  val system: ActorSystem = ActorSystem("askSystem")
//  val authManager = system.actorOf(Props[AuthManager])
//  authManager ! RegisterUser("alex", "abc")
//  authManager ! Authenticate("alex", "abd")


  class PipedAuthManager extends AuthManager {
    import AuthManager._

    override def handleAuthentication(username: String, password: String): Unit = {
      val future = authDb ? Read(username) //Future[Any]
      val passwordFuture = future.mapTo[Option[String]] //Future[Option[String]]
      val responseFuture = passwordFuture.map{
        case None =>
          log.info("None")
          AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Some(dbPassword) =>
          log.info("Some")
          if (dbPassword == password) AuthSuccess
          else AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
      }

      //When the future completes, send the response to the actor ref in the arg list
      responseFuture.pipeTo(sender())
    }
  }

  val pipedAuthManager = system.actorOf(Props[PipedAuthManager])
  pipedAuthManager ! RegisterUser("alex", "abc")
  pipedAuthManager ! Authenticate("alex", "abd")
}
