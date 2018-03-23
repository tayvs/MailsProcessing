package org.emis.tayvs

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global



object Distinct extends App {
  
  val db = new MongoDB("mails")
  
  db.distinctByField("domain")
    .andThen {
      case Success(v) => println(v.size)
      case Failure(ex) => ex.printStackTrace()
    }
  
}
