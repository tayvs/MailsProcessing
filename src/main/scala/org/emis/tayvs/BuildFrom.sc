import scala.util.{Failure, Success, Try}
import scala.collection.breakOut
//import com.sksamuel.avro4s.AvroSchema
//import org.apache.avro.Schema
//
//sealed trait CustomerAction
//case class Click(link: String, customer: String = "avatar", time: Long = Long.MaxValue) extends CustomerAction
//case class ShowImg(link: String, customer: String = "avatar") extends CustomerAction
//case class DownloadFile(fileSource: String, customer: String = "avatar") extends CustomerAction
//
//val schema = AvroSchema[CustomerAction]
//schema.toString(true)
//
//Schema
//  .createUnion(AvroSchema[Click], AvroSchema[DownloadFile], AvroSchema[ShowImg])
//  .toString(true)


Try(12) match {
  case Failure(ex) =>
  case Success(values) =>
}

(1 to 1000)
  .map(_ % 10)
  .toSet: Set[Int]

(1 to 1000)
  .map(_ % 10)(breakOut): Set[Int]
