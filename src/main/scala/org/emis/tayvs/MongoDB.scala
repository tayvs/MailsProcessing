package org.emis.tayvs

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{MultiBulkWriteResult, WriteResult}
import reactivemongo.api.{Cursor, DefaultDB, MongoDriver}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoDB(collName: String) {
  
  val db: DefaultDB = new MongoDriver().connection(List("172.30.240.131:27017"))("csv")
  val mailsCollection: BSONCollection = db(collName)
  
  Thread.sleep(1000)
  
  def bulkInsert[T](entities: Traversable[T])(implicit bsonDocWriter: BSONDocumentWriter[T]): Future[MultiBulkWriteResult] =
    mailsCollection
        .insert(false)
        .many(entities.toIterable)
  
  def insert[T](entity: T)(implicit bSONDocumentWriter: BSONDocumentWriter[T]): Future[WriteResult] =
    mailsCollection
      .insert(entity)
  
  def getByDomain[T: BSONDocumentReader](domain: String): Future[List[T]] =
    mailsCollection
      .find(BSONDocument("domain" -> domain))
      .cursor[T]()
      .collect(Int.MaxValue, Cursor.FailOnError[List[T]]())
  
  def deleteByDomain(domain: String): Future[WriteResult] =
    mailsCollection
      .remove(BSONDocument("domain" -> domain))
  
  
  def distinctByField(field: String): Future[List[BSONDocument]] =
    mailsCollection.distinct[BSONDocument, List](field)
}
