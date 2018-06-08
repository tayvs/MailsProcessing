package org.emis.tayvs.actors.FSM

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}


class AcctListenerV2Test(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with Matchers with FreeSpecLike with BeforeAndAfterAll {
  import org.emis.tayvs.actors.FSM.AcctListenerV2.{State, Data}
  
  
  override def afterAll(): Unit = {
    _system.shutdown()
    _system.awaitTermination(10.seconds)
  }
  
  "AcctListener " must {
    "start with NotInit state and Empty data" in {
      val acctListener = TestFSMRef(new AcctListenerV2())
      acctListener.stateName should be State.NotInit
      acctListener.stateData should be Data.Empty
    }
  }
  
}
