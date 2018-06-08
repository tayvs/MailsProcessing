package org.emis.tayvs.actors.FSM

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, FreeSpecLike, Matchers}
import scala.concurrent.duration._
import scala.language.postfixOps

class AcctListenerV2Test(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with Matchers with FreeSpecLike with BeforeAndAfterAll {
  import org.emis.tayvs.actors.FSM.AcctListenerV2._
  
  override def afterAll(): Unit = {
    _system.terminate()
  }
  
  "AcctListener " - {
    
    "start with NotInit state and Empty data" in new TestedAcctListener {
      acctListener.stateName shouldBe AcctState.FileNotInit
      acctListener.stateData shouldBe AcctData.Empty
    }
  }
  
  class TestedAcctListener {
    val acctListener = TestFSMRef(new AcctListenerV2())
  }
  
}


