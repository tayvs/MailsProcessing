package org.emis.tayvs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import scala.util.{Failure, Success}
import scalaj.http.{Http, HttpResponse}
import scala.concurrent.ExecutionContext.Implicits.global

object DomainResponse extends App {
  
  val sys = ActorSystem("csv")
  implicit val mat: ActorMaterializer = ActorMaterializer()(sys)
  
  val source: Source[String, NotUsed] = Source.fromIterator[String](() => List("http://google.com").iterator)
  
  def httpIsAlive(domain: String): Boolean = {
    
    def httpReq(domain: String, depth: Int = 10, tries: Int = 3): HttpResponse[String] = {
      val resp: HttpResponse[String] = Http(domain).asString
      if (resp.isSuccess || depth == 0) resp
      else resp.location
        .map(httpReq(_, depth - 1))
        .getOrElse(resp)
    }
    
    httpReq(domain).isSuccess
  }
  
  def endDomain(domain: String): Option[String] = {
    def httpReq(domain: String, depth: Int = 10, tries: Int = 3): HttpResponse[String] = {
      val resp: HttpResponse[String] = Http(domain).asString
      if (resp.isSuccess || depth == 0) resp
      else resp.location
        .map(httpReq(_, depth - 1))
        .getOrElse(resp)
    }
    
    httpReq(domain).location
  }
  
  source
    .via(Flow.fromFunction(domain => domain -> httpIsAlive(domain)))
    .runForeach(println)
    .andThen {
      case Failure(ex) => ex.printStackTrace()
      case Success(value) => println(value)
    }
  
}
