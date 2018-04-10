package org.emis.tayvs.actors

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorSystem, Identify, Props}
import com.typesafe.config.ConfigFactory

object PingStructure extends App {
  
  val sys = ActorSystem("ping")
  
  val actorSystemName = ConfigFactory.load().getConfig("app").getString("remoteActorSystem")
  sys.actorOf(Props(new Pinger(actorSystemName)), "pinger")
  
  class Pinger(actorSystemPath: String) extends Actor with ActorLogging {
    
    log.warning(s"start with actor system {}", actorSystemPath)
    
    context.actorSelection(actorSystemPath) ! Identify("ping")
    
    override def receive: Receive = {
      case ActorIdentity(_, Some(path)) => log.warning("actorPath {}", path)
    }
  }
  
}
