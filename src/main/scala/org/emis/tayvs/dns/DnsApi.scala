package org.emis.tayvs.dns

import javax.naming.NamingException

class DnsApi(private val lookUpper: DnsLookuper) extends DnsLookup with MXLookup with ALookup {
  
  @throws[NamingException]
  def doLookup(hostName: String, recordType: Array[String]): Array[Array[String]] =
    lookUpper.lookup(hostName, recordType)
  
}
