package org.emis.tayvs.actors.persistence

import akka.actor.ActorRef
import akka.persistence.{PersistentActor, SnapshotOffer}

import org.emis.tayvs.actors.persistence.SessionExample.SessionSupervisor._

object SessionExample extends App {
  
  class SessionSupervisor extends PersistentActor with SessionManager {
    override def persistenceId: String = self.path.name
    
    var sessionList: Map[Symbol, ((Seq[String], Int))] = Map.empty
    
    override def receiveRecover: Receive = {
      case SnapshotOffer(_, snapshot: Map[Symbol, ((Seq[String], Int))]) => sessionList = snapshot
      case _ =>
    }
    
    override def receiveCommand: Receive = {
      
      case Login(login) => getSession(login)
        .foreach(sessionInfo => self ! CreateSession(login, sessionInfo))
      
      case CreateSession(login, sessionInfo) =>
        persist(SessionCreated(Symbol(login), sessionInfo))(updateState)

      case Logout(login) => ???
      
      case _ =>
    }
    
    def updateState(ev: Event): Unit = ev match {
      case CreateSession(login, sessionInfo) => sessionList + (Symbol(login) -> sessionInfo)
    }
  }
  
  object SessionSupervisor {
    
    case class Login(login: String)
    case class Logout(login: String)
    case class CreateSession(login: String, sessionInfo: (Seq[String], Int))
    
    sealed trait Event
    case class SessionCreated(
      login: Symbol, sessionInfo: (Seq[String], Int), creationTime: Long = System.currentTimeMillis()
    ) extends Event
  }
  
}

trait SessionManager {
  
  val sessions: Map[Symbol, (Seq[String], Int)] = Map(
    'avatar -> (Seq("Get", "Post", "Put") -> 600)
  )
  
  def getSession(login: String): Option[(Seq[String], Int)] = sessions.get(Symbol(login))
  
}