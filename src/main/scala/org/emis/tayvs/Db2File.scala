package org.emis.tayvs

import java.nio.file.Paths
import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.stream.{ActorMaterializer, IOResult, OverflowStrategy}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink}
import akka.util.ByteString
import reactivemongo.bson.{BSONDocument, BSONString}
import scala.concurrent.Future

object Db2File extends App {
  
  implicit val system = ActorSystem("DomainInfo")
  implicit val mat = ActorMaterializer()
  implicit val disp = system.dispatchers.lookup("my-dispatcher")
  
  val domainsDB = new MongoDB("domains2")
  
  def lineSink(fileName: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)
  
  val res = domainsDB
    .getAll(
      BSONDocument("domain" -> 1),
      BSONDocument("mxRecords" -> BSONDocument("$ne" -> Array.empty[Int]))
    )
    .buffer(10000, OverflowStrategy.backpressure)
    .grouped(1000)
    .mapAsyncUnordered(20)(batch => Future(ByteString(
      batch.foldLeft(new StringBuilder())((acc, str) => acc
        .append(str.getAs[BSONString]("domain").get.value)
        .append("\n"))
        .toString()
    )))
    .toMat(FileIO.toPath(Paths.get("C://Porno/domains.txt")))(Keep.right)
    .run()
//    .runWith(lineSink("C://Porno/domains.txt"))
  
  res
    .map(res => println(s"writing end $res"))
  
}
