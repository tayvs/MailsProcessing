package org.emis.tayvs.actors.MDCLogging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.sift.AbstractDiscriminator

class CustomDoubleDiscriminator extends AbstractDiscriminator[ILoggingEvent] {
  
  override def getDiscriminatingValue(e: ILoggingEvent): String = {
    val campaignId: String = e.getMDCPropertyMap.get("strategy")
    val jobId: String = e.getMDCPropertyMap.get("num")
    s"$campaignId/$jobId/$jobId"
  }
  override def getKey: String = "strategyAndNum"
  
}
