package org.emis.tayvs

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import reactivemongo.bson.{BSONDocument, BSONDocumentHandler, BSONHandler, BSONString, Macros}
import scala.concurrent.Future
import scala.util.{Failure, Success}

import org.emis.tayvs.Distinct.db
import org.emis.tayvs.dns.{DnsApi, DnsLookuperImpl}

object CheckDomain extends App {
  
  implicit val system = ActorSystem("DomainInfo")
  implicit val mat = ActorMaterializer()
  implicit val disp = system.dispatcher
  
  val mxChecher = new DnsApi(new DnsLookuperImpl)
  val checkDomain: String => Future[Option[String]] = domain => Future(DomainResponse.endDomain(domain))
  
  def getMailDomainData(domain: String): Future[MailDomainInfo] = {
    val mxRecord = Future(mxChecher.doLookupMX(domain))
    val httpDomain = checkDomain(domain)
    (mxRecord zipWith httpDomain) ((mx, httpDom) => MailDomainInfo(domain, httpDom, mx.toList))
  }
  
  val domainsDB = new MongoDB("domains")
  
  db
    .getAll(BSONDocument("domain" -> 1))
    .collect { case bson: BSONDocument if bson.contains("domain") => bson.getAs[BSONString]("domain").get.value }
    .mapAsyncUnordered(100)(getMailDomainData)
    .async
    .mapAsyncUnordered(100)(ek => domainsDB.update(("domain", ek.domain), ek))
    .grouped(100)
    .runForeach(res => println("processed success %s, failed %s".format(res.span(_.ok).productIterator.toSeq: _*)))
    .andThen {
      case Success(Done) => println("stream end successfully")
      case Failure(ex) => ex.printStackTrace()
    }
}

case class MailDomainInfo(domain: String, destinationDomain: Option[String], mxRecords: List[String])
object MailDomainInfo {
  implicit val bsonHandler: BSONDocumentHandler[MailDomainInfo] = Macros.handler[MailDomainInfo]
}
