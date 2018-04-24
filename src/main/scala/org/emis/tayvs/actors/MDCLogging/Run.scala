package org.emis.tayvs.actors.MDCLogging

import akka.actor.{Actor, ActorSystem, DiagnosticActorLogging, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object Run extends App {

  val system = ActorSystem("MDCLogging", ConfigFactory.load().getConfig("MDCLogging"))
  system.actorOf(Props[Ping], "PingActor")
  
}

class Ping extends Actor with DiagnosticActorLogging {
  import context.dispatcher

  log.error(new Throwable(s"$self started"), "Actor started")
  context.system.scheduler.schedule(5 seconds, 5 seconds, self, "Hello")

  override def receive: Receive = {
    case any => log.warning(s"$any arrived")
  }
}


class Pong extends Actor with DiagnosticActorLogging {
  override def receive: Receive = ???
}
