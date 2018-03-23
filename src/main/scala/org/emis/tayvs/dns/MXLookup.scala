package org.emis.tayvs.dns

trait MXLookup {
  this: DnsLookup =>
  
  def doLookupMX(hostName: String): Array[String] = doLookup(hostName, "MX")
  
}
