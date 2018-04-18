import com.sksamuel.avro4s.AvroSchema
import org.apache.avro.Schema

sealed trait CustomerAction
case class Click(link: String, customer: String = "avatar", time: Long = Long.MaxValue) extends CustomerAction
case class ShowImg(link: String, customer: String = "avatar") extends CustomerAction
case class DownloadFile(fileSource: String, customer: String = "avatar") extends CustomerAction

val schema = AvroSchema[CustomerAction]
schema.toString(true)

Schema
  .createUnion(AvroSchema[Click], AvroSchema[DownloadFile], AvroSchema[ShowImg])
  .toString(true)

