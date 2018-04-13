package org.emis.tayvs.actors.kafka

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.sksamuel.avro4s._
import org.apache.avro.Schema

object AvroSerialization extends App {

  sealed trait CustomerAction
  case class Click(link: String, customer: String = "avatar", time: Long = Long.MaxValue) extends CustomerAction
  case class ShowImg(link: String, customer: String = "avatar") extends CustomerAction
  case class DownloadFile(fileSource: String, customer: String = "avatar") extends CustomerAction

  case class Link(link: String)
  
  val caSchema: Schema = AvroSchema[Click]
  println(caSchema.toString(true))

  val ca = Click("SomeHref")

  val bytes = AvroSerializator.write(ca)
  val deserCA = AvroSerializator.read[Click](bytes)
  val link = AvroSerializator.read[Link](bytes)
  
  ("click", bytes) match {
    case ("click", bytes) => AvroSerializator.read[Click](bytes)
    case ("showImg", bytes) => AvroSerializator.read[ShowImg](bytes)
    case ("downloadFile", bytes) => AvroSerializator.read[DownloadFile](bytes)
  }
  
  println(ca)
  println(deserCA)
  println(ca == deserCA)
  
  println(link)

}

object AvroSerializator {

  def write[T](ent: T)(implicit schemaFor: SchemaFor[T], toRecord: ToRecord[T]): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[T](baos)
    output.write(ent)
    output.close()
    baos.toByteArray
  }

  def read[T](bytes: Array[Byte])(implicit schemaFor: SchemaFor[T], fromRecord: FromRecord[T]): T = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[T](in)
    input.iterator.toSeq.head
  }

}
