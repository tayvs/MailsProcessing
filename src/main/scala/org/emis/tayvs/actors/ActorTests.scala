package org.emis.tayvs.actors

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, DiagnosticActorLogging, OneForOneStrategy, Props,
  SupervisorStrategy, Terminated}
import akka.event.Logging.MDC
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object ActorTests extends App {
  
  class Supervisor extends Actor with ActorLogging {
    
    context.watch(context.actorOf(Props[SupStratagyTest], "SupStratagyTest"))
    
    override def receive: Receive = {
      case Terminated(actorRef) => println(s"child were terminated $actorRef")
      case any => println(any)
    }
    
    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(3, 0.5 seconds, false) {
      case ex: Exception =>
        println(ex.getMessage)
        Restart
//        Resume
    }
  }
  
  class SupStratagyTest extends Actor with DiagnosticActorLogging {
    
    override def mdc(currentMessage: Any): MDC = Map("strategy" -> self.path.name)
    
    implicit val ex: ExecutionContextExecutor = context.dispatcher
    val sch: Cancellable = context.system.scheduler.schedule(0 second, 1 second, self, "ping")
    
    override def postStop(): Unit = {
      super.postStop()
      sch.cancel()
    }
    
    override def receive: Receive = {
      case "ping" => log.warning("ping")
      case _ =>
        println("Exception throwned")
        throw new Exception("Exception thrown")
    }
    
  }
  
  val system = ActorSystem("test")
  //  system.actorOf(Props[Supervisor], "supervisor")
  val strategy = List(
    system.actorOf(Props[SupStratagyTest], "strategy1"),
    system.actorOf(Props[SupStratagyTest], "strategy2"),
    system.actorOf(Props[SupStratagyTest], "strategy3")
  )
  
//  strategy.foreach(_ ! "ping")

}
