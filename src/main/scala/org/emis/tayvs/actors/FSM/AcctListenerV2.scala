package org.emis.tayvs.actors.FSM

import java.io.File
import akka.actor.{Actor, DiagnosticActorLogging, FSM}

import org.emis.tayvs.actors.FSM.AcctListenerV2.{AcctData, AcctState}

class AcctListenerV2 extends Actor with FSM[AcctState, AcctData] /*with DiagnosticActorLogging */{
  import AcctListenerV2._
  
  startWith(AcctState.FileNotInit, AcctData.Empty)
  
  when(AcctState.FileNotInit) {FSM.NullFunction}
  when(AcctState.FileInit) {FSM.NullFunction}
  

  
}
object AcctListenerV2 {
  sealed trait AcctState
  object AcctState {
    case object FileNotInit extends AcctState
    case object FileInit extends AcctState
  }
  
  sealed trait AcctData
  object AcctData {
    case object Empty extends AcctData
    case class InitFile(file: File) extends AcctData
  }
}
