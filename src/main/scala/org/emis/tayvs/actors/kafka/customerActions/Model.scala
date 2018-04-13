package org.emis.tayvs.actors.kafka.customerActions

object Model {
  
  case class Click(link: String, customer: String = "avatar", time: Long = System.currentTimeMillis())
  case class ShowImg(link: String, customer: String = "avatar", time: Long = System.currentTimeMillis())
  case class DownloadFile(fileSource: String, customer: String = "avatar", time: Long = System.currentTimeMillis())
  
}
