package org.emis.tayvs.dns

trait ALookup {
  this: DnsLookup =>
  
  def doLookupA(hostName: String): Array[String] = doLookup(hostName, "A")
  
}
