package org.emis.tayvs.actors.kafka.customerActions

import reactivemongo.bson.BSONDateTime

object Model {
  
  case class Click(link: String, customer: String = "avatar", time: Long = System.currentTimeMillis())
  case class ShowImg(link: String, customer: String = "avatar", time: Long = System.currentTimeMillis())
  case class DownloadFile(fileSource: String, customer: String = "avatar", time: Long = System.currentTimeMillis())
  
  case class CustomerAction(
    id: Option[String] = None,
    emailId: String,
    campaignId: String,
    creativeId: String,
    offerId: String,
    subjectId: String,
    fromNameId: String,
    domain: String,
    remoteAdr: String,
    action: String,
    activeKey: String,
    serverId: String,
    dateTime: BSONDateTime,
    headers: Seq[String]
  )
  
}
