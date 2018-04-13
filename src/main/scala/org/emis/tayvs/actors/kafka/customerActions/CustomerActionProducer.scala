package org.emis.tayvs.actors.kafka.customerActions

import java.util

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization
import org.apache.kafka.common.serialization.{ByteArraySerializer, Serializer, StringSerializer}
import org.emis.tayvs.actors.kafka.AvroSerializator

import scala.concurrent.duration._
import scala.language.postfixOps

object CustomerActionProducer extends App {
  
  implicit val system = ActorSystem(
    "kafka_customerActions_producer",
    ConfigFactory.load.getConfig("kafka_customerActions_producer")
      .withFallback(ConfigFactory.load.getConfig("akka.kafka.producer")))
  implicit val mat = ActorMaterializer()
  
  val customerActionsTopic = "customerAction"

//  case class CustomerAction(action: String)
  
  val producerSettings = ProducerSettings(system, new StringSerializer, new Serializer[Array[Byte]] {
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}
    override def serialize(topic: String, data: Array[Byte]): Array[Byte] = data
    override def close(): Unit = {}
  })
    .withBootstrapServers("localhost:9092")
  
  Source(1 to 20)
    .delay(1 seconds)
    .map(num =>
      if (num % 2 == 0) "click" -> AvroSerializator.write(Model.Click("ClickLink" + num))
      else "img" -> AvroSerializator.write(Model.ShowImg("ImgLink" + num))
    )
    .map {
      case (key, bytes) =>
        println(key, bytes)
        new ProducerRecord[String, Array[Byte]](customerActionsTopic, key, bytes)
    }
    .runWith(Producer.plainSink(producerSettings))
  
}
