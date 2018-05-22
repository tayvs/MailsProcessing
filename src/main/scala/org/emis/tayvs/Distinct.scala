package org.emis.tayvs

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{ActorMaterializer, Attributes, OverflowStrategy}
import akka.{Done, NotUsed}
import reactivemongo.bson.{BSONDocument, BSONElement, BSONString}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

import scala.collection.breakOut

object Distinct extends App {
  
  implicit val system = ActorSystem("DomainsDistinct"/*, ConfigFactory.load().getConfig("app")*/)
  implicit val mat = ActorMaterializer()
  implicit val ex = system.dispatcher
  
  val db = new MongoDB("mails")
  
  val domainsDB = new MongoDB("domains2")
  domainsDB.makeIndex("domain")
  
  //  db
//    .getAll(BSONDocument("domain" -> 1))
//    .grouped(10000)
//    .mapAsyncUnordered(20)(doms =>
//      domainsDB.bulkInsert(doms.map(bson => bson.remove("_id"))(breakOut): Set[BSONDocument])
//        .andThen {
//          case Failure(ex) => ex.printStackTrace()
//        }
//    )
//    .runForeach(v => println(s"${System.currentTimeMillis()} $v"))
//    .andThen {
//      case Success(Done) => println("stream end successfully")
//      case Failure(ex) => ex.printStackTrace()
//    }
  
  def getDomain(bson: BSONDocument): Option[String] =
    bson.stream.collectFirst { case Success(BSONElement("domain", BSONString(domain))) => domain }
  def getBsonElement(bson: BSONDocument, bsonElCheck: BSONElement => Boolean): Option[BSONElement] =
    bson.stream.collectFirst { case Success(el) if bsonElCheck(el) => el }
  
  val domainBSONElementCheck = (el: BSONElement) => {
    el.name.equals("domain") && el.value.isInstanceOf[BSONString]
  }
  
  println(s"start ${System.currentTimeMillis()}")
  db
    .getAll(BSONDocument("domain" -> 1))
//    .take(100000)
    .buffer(500000, OverflowStrategy.backpressure)
//  Source.fromIterator(() => List(BSONDocument("domain" -> "google.com")).iterator)
    .grouped(20000)
    .mapAsyncUnordered(20)(bsons => Future(
      bsons.par
        .groupBy(getBsonElement(_, domainBSONElementCheck))
        .collect { case (Some(bsonEl), els) => (bsonEl, els.size) }
    ))
//    .mapConcat(identity)
    .async
//    .via(
//      reduceByKey[BSONElement, BSONElement, Int](
//        maximumGroupSize = Integer.MAX_VALUE,
//        groupKey = identity,
//        map = _ => 1
//      )(
//        0,
//        (left: Int, right: Int) ⇒ left + right
//      )
//    )
//    .grouped(10000)
//    .mapAsyncUnordered(20)(bsons => Future(bsons.groupBy(identity).map(el => el._1 -> el._2.size)))
    .fold(
    Map.empty[BSONElement, Int].par
  ) { (acc, el) =>
    println(s"${el.size} new elements")
    el.foldLeft(acc)((acc2, el2) => acc2.updated(el2._1, acc2.getOrElse(el2._1, 0) + el2._2))
  }
    .mapAsyncUnordered(1) { domsAndNums =>
      println(s"bulk insert of ${domsAndNums.size} domains started")
      domainsDB
        .bulkInsert(domsAndNums
          .map { case (domainBsonEl, count) => BSONDocument(domainBsonEl, "count" -> count) }(breakOut)
          : Seq[BSONDocument]
        )
        .andThen { case Failure(ex) => ex.printStackTrace() }
    }
    .runForeach(v => println(s"${System.currentTimeMillis()} $v"))
    .andThen {
      case Success(Done) => println(s"stream end successfully ${System.currentTimeMillis()}")
      case Failure(ex) => ex.printStackTrace()
    }
  
  def reduceByKey[In, K, Out](
    maximumGroupSize: Int,
    groupKey: (In) ⇒ K,
    map: (In) ⇒ Out
  )(
    reduceDef: Out,
    reduce: (Out, Out) ⇒ Out
  ): Flow[In, (K, Out), NotUsed] = {
    
    Flow[In]
      .groupBy[K](maximumGroupSize, groupKey)
      .grouped(10000)
      .map(els => groupKey(els.head) -> els.foldLeft(reduceDef)((acc, el) => reduce(acc, map(el))))
//      .map(e ⇒ groupKey(e) -> map(e))
      .reduce((l, r) ⇒ l._1 -> reduce(l._2, r._2))
      .mergeSubstreams
  }
}
