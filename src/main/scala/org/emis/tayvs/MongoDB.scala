package org.emis.tayvs

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{MultiBulkWriteResult, UpdateWriteResult, WriteResult}
import reactivemongo.api.{Cursor, DefaultDB, MongoDriver}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.akkastream.{State, cursorProducer}
import reactivemongo.api.indexes.{Index, IndexType}

class MongoDB(collName: String) {
  
  val db: DefaultDB = new MongoDriver().connection(List("172.30.240.131:27017"))("csv")
  val coll: BSONCollection = db(collName)
  
  Thread.sleep(1000)
  println(s"Connection to $collName inited")
  
  def bulkInsert[T: BSONDocumentWriter](entities: Traversable[T]): Future[MultiBulkWriteResult] =
    coll
      .insert(false)
      .many(entities.toIterable)
  
  def insert[T](entity: T)(implicit bSONDocumentWriter: BSONDocumentWriter[T]): Future[WriteResult] =
    coll
      .insert(entity)
  
  def update[T: BSONDocumentWriter](key: (String, String), ent: T): Future[UpdateWriteResult] =
    coll
      .update(
        selector = BSONDocument(key),
        update = BSONDocument("$set" -> ent)
      )
  
  def getByDomain[T: BSONDocumentReader](domain: String): Future[List[T]] =
    coll
      .find(BSONDocument("domain" -> domain))
      .cursor[T]()
      .collect(Int.MaxValue, Cursor.FailOnError[List[T]]())
  
  def deleteByDomain(domain: String): Future[WriteResult] =
    coll
      .remove(BSONDocument("domain" -> domain))
  
  def distinctByField[M[_] <: Iterable[_]](field: String)(implicit cbf: CanBuildFrom[M[_], BSONDocument,
    M[BSONDocument]]): Future[M[BSONDocument]] =
    coll.distinct[BSONDocument, M](field)
  
  def getAll(projection: BSONDocument, selector: BSONDocument = BSONDocument.empty)(implicit mat: Materializer): Source[BSONDocument,
    Future[State]] =
    coll
      .find(
        selector,
        projection = projection
      )
      .cursor[BSONDocument]()
      .documentSource()
  
  def makeIndex(fields: String*): Future[Boolean] = {
    coll.indexesManager.ensure(Index(fields.map(field => (field, IndexType.Text))))
  }
  
}
