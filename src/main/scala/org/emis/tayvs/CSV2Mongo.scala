package org.emis.tayvs

import java.io.File
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.github.tototoshi.csv.CSVParser
import com.typesafe.config.{Config, ConfigFactory}
import reactivemongo.bson.{BSONDocumentHandler, Macros}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io
import scala.util.Failure
import scala.collection.JavaConverters._

object CSV2Mongo extends App {
  
  val inputFile = "src/main/resources/Mailable_2017_new_oth_20170807_US_New.csv"
//  val inputFile = "src/main/resources/Mailable_2017_new_oth_20170807_US_9.csv"
  println(new File(inputFile).getAbsolutePath)
  println(new File(inputFile).exists())
  
  val badDomains = ConfigFactory.load().getConfig("app").getStringList("badDomains").asScala.toSet
  
  val sys = ActorSystem("csv")
  implicit val mat: ActorMaterializer = ActorMaterializer()(sys)
  
  val db = new MongoDB("mails")
  val dbBadMails = new MongoDB("badMails")
  
  Source
    .fromIterator[String](() => io.Source.fromFile(inputFile).getLines())
    .mapAsync(1000)(str => Future(
      CSVParser.parse(str, '\\', ';', '"')
        .collect { case _@email :: country :: ip :: provider :: _ => OutputLine(email, country, ip, provider) }
    ))
    .collect { case Some(v) => v }
    .grouped(10000)
    .mapAsyncUnordered(2) { ent =>
      db.bulkInsert(ent).andThen { case Failure(ex) => ex.printStackTrace() }
    }
    .toMat(Sink.foreach(el => println(s"${System.currentTimeMillis()} $el")))(Keep.right)
    .run()
    .foreach(println)
  
  case class OutputLine(email: String, country: String, ip: String, provider: String, domain: String)
  object OutputLine {
    
    implicit val mongoHandler: BSONDocumentHandler[OutputLine] = Macros.handler[OutputLine]
    
    def substringFromChar(str: String, ch: Char): String = str.substring(str.indexOf(ch) + 1)
    def apply(email: String, country: String, ip: String, provider: String, domain: String): OutputLine = new
        OutputLine(email, country, ip, provider, domain)
    def apply(email: String, country: String, ip: String, provider: String): OutputLine =
      new OutputLine(email, country, ip, provider, substringFromChar(email, '@'))
//    def apply(inputLine: InputLine): OutputLine = new OutputLine(
//      inputLine.email, inputLine.country, inputLine.ip, inputLine.provider, substringFromChar(inputLine.email, '@'))
  }
}
