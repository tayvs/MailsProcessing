package org.emis.tayvs.actors.kafka.customerActions

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import scala.concurrent.duration._

object CustomerActionProducer extends App {
  
  implicit val system = ActorSystem(
    "kafka_customerActions_producer",
    ConfigFactory.load.getConfig("kafka_customerActions_producer")
  .withFallback(ConfigFactory.load.getConfig("akka.kafka.producer")))
  implicit val mat = ActorMaterializer()
  
  val customerActionsTopic = "customerAction"
  
  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new ByteArraySerializer)
    .withBootstrapServers("localhost:9092")
  
  case class CustomerAction(action: String)
  
  Source(1 to 20)
    .delay(1 seconds)
    .map(num => CustomerAction("customerAction" + num))
    .map { elem =>
      println(elem)
      new ByteArraySerializer().serialize((customerActionsTopic, elem))
//      new ProducerRecord[Array[Byte], Array[Byte]](customerActionsTopic, elem)
    }
    .runWith(Producer.plainSink(producerSettings))
  
}
