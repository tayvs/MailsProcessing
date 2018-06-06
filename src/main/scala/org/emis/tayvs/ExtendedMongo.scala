package org.emis.tayvs

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import reactivemongo.akkastream.State
import reactivemongo.bson.BSONDocument
import reactivemongo.akkastream.{State, cursorProducer}
import scala.concurrent.Future

trait ExtendedMongo {
  this: MongoDB =>
  
  def getAllWithSort(
    projection: BSONDocument, selector: BSONDocument = BSONDocument.empty, sort: BSONDocument = BSONDocument.empty
  )(implicit mat: Materializer): Source[BSONDocument, Future[State]] =
    coll
      .find(
        selector,
        projection = projection
      )
      .sort(sort)
      .cursor[BSONDocument]()
      .documentSource()
  
}
