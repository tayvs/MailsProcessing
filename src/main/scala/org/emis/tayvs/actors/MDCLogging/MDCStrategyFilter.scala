package org.emis.tayvs.actors.MDCLogging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class MDCStrategyFilter extends Filter[ILoggingEvent] {
  override def decide(event: ILoggingEvent): FilterReply = {
    if (event.getMDCPropertyMap.containsKey("strategy")) FilterReply.ACCEPT
    else FilterReply.DENY
  }
  
}