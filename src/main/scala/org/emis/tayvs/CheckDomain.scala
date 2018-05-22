package org.emis.tayvs

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import javax.naming.NameNotFoundException
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson._
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import org.emis.tayvs.dns.{DnsApi, DnsLookuperImpl}

object CheckDomain extends App {
  
  implicit val system = ActorSystem("DomainInfo")
  implicit val mat = ActorMaterializer()
  implicit val disp = system.dispatcher
  
  val mxChecher = new DnsApi(new DnsLookuperImpl)
  val checkDomain: String => Future[Option[String]] = domain => Future(DomainResponse.endDomain(domain))
  
  def retryOp[T](op: => Future[T], tries: Int = 3, exOpt: Option[Throwable] = None): Future[T] = {
    op
      .recoverWith {
        case ex: NameNotFoundException =>
          Future.failed(ex)
        case t: Throwable =>
          if (tries == 0) Future.failed(exOpt.map(ex => t.initCause(ex)).getOrElse(t))
          else retryOp(op, tries - 1, exOpt.map(t.initCause).orElse(Some(t)))
      }
  }
  
  def getMailDomainData(domain: String): Future[MailDomainInfo] = {
    val mxRecord: Future[Array[String]] = retryOp(Future(mxChecher.doLookupMX(domain)))
      .recover { case t: Throwable =>
        println(t)
        Array.empty
      }
    
    val httpRecord: Future[Array[String]] = retryOp(Future(mxChecher.doLookupA("http://" + domain)))
      .recover { case t: Throwable =>
        println(t)
        Array.empty
      }
    val httpsRecord: Future[Array[String]] = retryOp(Future(mxChecher.doLookupA("https://" + domain)))
      .recover { case t: Throwable =>
        println(t)
        Array.empty
      }
    
    httpRecord
      .zipWith(httpsRecord)((ar1, ar2) => ar1 ++ ar2)
      .zipWith(mxRecord)((httpDom, mx) => MailDomainInfo(domain, httpDom, mx))
  }
  
  val domainsDB = new MongoDB("domains2")
  
  domainsDB
    .getAll(
      BSONDocument("domain" -> 1),
      BSONDocument("mxRecords" -> BSONDocument("$exists" -> false)))
//    .take(1)
    .buffer(100000, OverflowStrategy.backpressure)
    .mapAsyncUnordered(1000) { bson =>
      Future(bson.getAs[BSONString]("domain"))
        .flatMap(_.fold[Future[Option[UpdateWriteResult]]](Future.successful(None))(el =>
          getMailDomainData(el.value)
            .flatMap(el => domainsDB.update(("domain", el.domain), el))
            .map(Some(_))
        ))
    }
//    .collect { case bson: BSONDocument if bson.contains("domain") => bson.getAs[BSONString]("domain").get.value }
//    .mapAsyncUnordered(1000)(getMailDomainData)
//    .async
//    .mapAsyncUnordered(1000)(el => domainsDB.update(("domain", el.domain), el))
    .grouped(100)
    .runForeach { res =>
      val (okRes, failRes) = res.flatten.span(_.ok)
      println("processed success %s, failed %s".format(okRes.size, failRes.size))
    }
    .andThen {
      case Success(Done) => println("stream end successfully")
      case Failure(ex) =>
        println("Stream Exception")
        ex.printStackTrace()
    }
}

case class MailDomainInfo(domain: String, aRecords: Array[String], mxRecords: Array[String])
object MailDomainInfo {
  //  implicit val exHandler: BSONDocumentHandler[Exception] = Macros.handler[Exception]
//  implicit val trySHandler: BSONDocumentHandler[Success[Array[String]]] = Macros.handler[Success[Array[String]]]
//  implicit val tryFHandler: BSONDocumentHandler[Failure[Throwable]] = Macros.handler[Failure[Throwable]]
implicit val bsonHandler: BSONDocumentHandler[MailDomainInfo] = Macros.handler[MailDomainInfo]
}
