package org.emis.tayvs

import com.typesafe.config.ConfigFactory
import reactivemongo.bson.BSONDocument
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

/**
  * Replace all mails with bad domain into another collection.
  * Bad domains this is unreached, don't exists etc domains
  */
object FilterByDomain extends App {
  
  val domains = ConfigFactory.load().getConfig("app").getStringList("badDomains").asScala.toSet
  
  val dbMails = new MongoDB("mails")
  val dbBadMails = new MongoDB("badMails")
  
  domains
    .map { domain =>
      for {
        badMails <- dbMails.getByDomain[BSONDocument](domain)
        insertRes <- dbBadMails.bulkInsert(badMails)
        deleteRes <- dbMails.deleteByDomain(domain) if insertRes.ok
      } yield {
        println(s"$domain bad mails count ${badMails.size}")
        println(s"$domain insert result ${insertRes}")
        println(s"$domain delete result ${deleteRes}")
        (domain, deleteRes.ok)
      }
    }
  
}
