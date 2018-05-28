package org.emis.tayvs

import akka.Done
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{ActorMaterializer, Attributes, OverflowStrategy}
import javax.naming.NameNotFoundException
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson._
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.collection.immutable
import scala.collection.breakOut
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

import org.emis.tayvs.dns.{DnsApi, DnsLookuperImpl}
import org.emis.tayvs.Utils.FutureExtensions

object CheckDomain extends App {
  
  implicit val system = ActorSystem("DomainInfo")
  implicit val mat = ActorMaterializer()
  implicit val disp = system.dispatchers.lookup("my-dispatcher")
  implicit val timeout = 200 millis
  
  val mxChecher = new DnsApi(new DnsLookuperImpl)
  val checkDomain: String => Future[Option[String]] = domain => Future(DomainResponse.endDomain(domain))
  
  def retryOp[T](op: () => Future[T], tries: Int = 5, exOpt: Option[Throwable] = None): Future[T] = {
    op()
      .recoverWith {
        case ex: NameNotFoundException =>
          Future.failed(ex)
        case t: Throwable =>
          if (tries == 0) Future.failed(exOpt.map(ex => t.initCause(ex)).getOrElse(t))
          else retryOp(op, tries - 1, exOpt.map(t.initCause).orElse(Some(t)))
      }
  }
  
  def getMailDomainData(domain: String): Future[MailDomainInfo] = {
    retryOp { () =>
//      val startTime = System.currentTimeMillis()
      Future(mxChecher.doLookupMX(domain))
//        .withTimeout(new Exception(s"lookup of $domain mx record timeout"))
//        .andThen { case _ => println(s"$domain try take ${System.currentTimeMillis() - startTime}") }
    }
      .transform((tr: Try[Array[String]]) =>
        tr
          .recover {
            case _:NameNotFoundException => Array.empty[String]
            case t: Throwable => println(domain, t); Array.empty[String]
          }
          .map { mxRec => MailDomainInfo(domain, mxRecords = mxRec) }
      )
  }
  
  val domainsDB = new MongoDB("domains2")
  
  domainsDB
    .getAllBulk(
      BSONDocument("domain" -> 1),
      BSONDocument("mxRecords" -> Array.empty[String]))
//    .take(1)
//    .collect{case Some(el) => el}
    .mapConcat(iter => iter.map(_.getAs[BSONString]("domain").get.value).to[immutable.Iterable])
    .buffer(10000, OverflowStrategy.backpressure)
//    .log("mapConcat")
//    .withAttributes(Attributes.logLevels(
//      onElement = Logging.WarningLevel,
//      onFinish = Logging.InfoLevel,
//      onFailure = Logging.ErrorLevel
//    ))
//    .mapAsyncUnordered(20) { bson =>
//    Future(bson.getAs[BSONString]("domain").get.value)
//      .andThen { case Success(domain) => println(s"$domain domain fetch") }
//  }
//    .async
//    .buffer(1000, OverflowStrategy.backpressure)
//    .mapAsyncUnordered(20) { el => getMailDomainData(el.value) }
//    .log("DnsCheck")
//    .withAttributes(Attributes.logLevels(
//      onElement = Logging.WarningLevel,
//      onFinish = Logging.InfoLevel,
//      onFailure = Logging.ErrorLevel
//    ))
//    .collect { case bson: BSONDocument if bson.contains("domain") => bson.getAs[BSONString]("domain").get.value }
//    .mapAsyncUnordered(1000)(getMailDomainData)
//    .async
//    .mapAsyncUnordered(1000)(el => domainsDB.update(("domain", el.domain), el))
    .async
    .mapAsyncUnordered(250)(getMailDomainData)
    .filter(_.aRecords.nonEmpty)
    .mapAsyncUnordered(250)(el => domainsDB.update(("domain", el.domain), el))
    .grouped(100)
    .runForeach { res =>
      val (okRes, failRes) = res.span(_.ok)
      println("processed success %s, failed %s".format(okRes.size, failRes.size))
    }
    .andThen {
      case Success(Done) => println("stream end successfully")
      case Failure(ex) =>
        println("Stream Exception")
        ex.printStackTrace()
    }
}

case class MailDomainInfo(domain: String, aRecords: Array[String] = Array.empty, mxRecords: Array[String] = Array.empty)
object MailDomainInfo {
  //  implicit val exHandler: BSONDocumentHandler[Exception] = Macros.handler[Exception]
//  implicit val trySHandler: BSONDocumentHandler[Success[Array[String]]] = Macros.handler[Success[Array[String]]]
//  implicit val tryFHandler: BSONDocumentHandler[Failure[Throwable]] = Macros.handler[Failure[Throwable]]
implicit val bsonHandler: BSONDocumentHandler[MailDomainInfo] = Macros.handler[MailDomainInfo]
}
