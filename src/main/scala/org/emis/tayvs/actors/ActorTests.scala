package org.emis.tayvs.actors

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, OneForOneStrategy, Props, SupervisorStrategy,
  Terminated}
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
  
  class SupStratagyTest extends Actor with ActorLogging {
    
    implicit val ex: ExecutionContextExecutor = context.dispatcher
    val sch: Cancellable = context.system.scheduler.schedule(0 second, 1 second, self, "")
    
    
    override def postStop(): Unit = {
      super.postStop()
      sch.cancel()
    }
    
    override def receive: Receive = {
      case _ =>
        println("Exception throwned")
        throw new Exception("Exception thrown")
    }
    
  }
  
  val system = ActorSystem("test")
  system.actorOf(Props[Supervisor], "supervisor")
  
}
