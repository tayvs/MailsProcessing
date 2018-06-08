package org.emis.tayvs.actors.FSM

import java.io.File
import akka.actor.{Actor, DiagnosticActorLogging, FSM}

import org.emis.tayvs.actors.FSM.AcctListenerV2.{Data, State}

class AcctListenerV2 extends Actor with FSM[State, Data] /*with DiagnosticActorLogging */{
  import AcctListenerV2._
  
  startWith(State.FileNotInit, Data.Empty)
  
  when(State.FileNotInit) {FSM.NullFunction}
  when(State.FileInit) {FSM.NullFunction}
  
}
object AcctListenerV2 {
  sealed trait State
  object State {
    case object FileNotInit extends State
    case object FileInit extends State
  }
  
  sealed trait Data
  object Data {
    case object Empty extends Data
    case class InitFile(file: File) extends Data
  }
}
