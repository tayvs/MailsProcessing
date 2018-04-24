package org.emis.tayvs.actors.MDCLogging

import akka.actor.{Actor, DiagnosticActorLogging}

object Run extends App {
  
  class Ping extends Actor with DiagnosticActorLogging {
    override def receive: Receive = ???
  }
  class Pong extends Actor with DiagnosticActorLogging {
    override def receive: Receive = ???
  }
  
}
