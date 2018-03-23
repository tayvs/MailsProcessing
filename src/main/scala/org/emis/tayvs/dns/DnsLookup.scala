package org.emis.tayvs.dns

import javax.naming.NamingException

trait DnsLookup {
  
  @throws[NamingException]
  def doLookup(hostName: String, recordType: String): Array[String] = doLookup(hostName, Array(recordType))(0)
  
  @throws[NamingException]
  def doLookup(hostName: String, recordType: Array[String]): Array[Array[String]]
  
}
