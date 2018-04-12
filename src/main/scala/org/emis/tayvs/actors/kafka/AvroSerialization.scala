package org.emis.tayvs.actors.kafka

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.sksamuel.avro4s._
import org.apache.avro.Schema

object AvroSerialization extends App {

  sealed trait CustomerAction
  case object Open extends CustomerAction
  case object Download extends CustomerAction
  case object Click extends CustomerAction

  case class Action(actionType: CustomerAction, customer: String = "avatar")

  val caSchema: Schema = AvroSchema[Action]
  println(caSchema.toString(true))

  val ca = Action(Open)

  val bytes = AvroSerializator.write(ca)
  val deserCA = AvroSerializator.read[Action](bytes)

  println(ca)
  println(deserCA)
  println(ca == deserCA)

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
