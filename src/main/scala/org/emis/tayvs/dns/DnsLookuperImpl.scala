package org.emis.tayvs.dns

import java.util
import javax.naming.NamingException
import javax.naming.directory.{Attribute, Attributes, DirContext, InitialDirContext}

class DnsLookuperImpl extends DnsLookuper {
  
  val env: util.Hashtable[String, String] = new util.Hashtable()
  env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory")
//  val ictx: DirContext = new InitialDirContext(env)
  
  implicit class AttributeArray(attr: Attribute) {
    def toStringArray: Array[String] = (0 until attr.size()).map(attr.get(_).toString).toArray
  }
  
  def getRecordByType(attrs: Attributes, recordType: String): Array[String] =
    Option(attrs.get(recordType))
      .map { attr => attr.toStringArray }
      .getOrElse(Array.empty)
  
  @throws[NamingException]
  override def lookup(hostName: String, recordType: Array[String]): Array[Array[String]] = {
    val ictx: DirContext = new InitialDirContext(env)
    val attrs: Attributes = ictx.getAttributes(hostName, recordType)
    recordType.map(attrType => getRecordByType(attrs, attrType))
  }
  
}
