package org.emis.tayvs.dns

import javax.naming.NamingException

trait DnsLookuper {
  
  @throws[NamingException]
  def lookup(hostName: String, recordType: Array[String]): Array[Array[String]]
  
}
