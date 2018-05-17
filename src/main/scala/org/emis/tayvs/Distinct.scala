package org.emis.tayvs

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import reactivemongo.bson.{BSONDocument, BSONString}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scala.collection.breakOut

object Distinct extends App {
  
  implicit val system = ActorSystem("DomainsDistinct"/*, ConfigFactory.load().getConfig("app")*/)
  implicit val mat = ActorMaterializer()
  
  val db = new MongoDB("mails")
  
  val domainsDB = new MongoDB("domains")
  domainsDB.makeIndex("domain")
  
  db
    .getAll(BSONDocument("domain" -> 1))
    .grouped(10000)
    .mapAsyncUnordered(20)(doms =>
      domainsDB.bulkInsert(doms.map(bson => bson.remove("_id"))(breakOut): Set[BSONDocument])
        .andThen {
          case Failure(ex) => ex.printStackTrace()
        }
    )
    .runForeach(v => println(s"${System.currentTimeMillis()} $v"))
    .andThen {
      case Success(Done) => println("stream end successfully")
      case Failure(ex) => ex.printStackTrace()
    }
  
  db
    .getAll(BSONDocument("domain" -> 1))
    .via(
      reduceByKey(
        MaximumDistinctWords,
        groupKey = (bson: BSONDocument) => bson.getAs[BSONString]("domain").get.value,
        map = _ => 1
      )(
        (left: Int, right: Int) ⇒ left + right
      )
    )
  )
  
  def reduceByKey[In, K, Out](
    maximumGroupSize: Int,
    groupKey: (In) ⇒ K,
    map: (In) ⇒ Out)(reduce: (Out, Out) ⇒ Out): Flow[In, (K, Out), NotUsed] = {
    
    Flow[In]
      .groupBy[K](maximumGroupSize, groupKey)
      .map(e ⇒ groupKey(e) -> map(e))
      .reduce((l, r) ⇒ l._1 -> reduce(l._2, r._2))
      .mergeSubstreams
  }
}
