package org.emis.tayvs.actors.kafka

import akka.actor.ActorSystem
import akka.kafka.{ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

object ProducerPilot extends App {
  
  implicit val system = ActorSystem("kafka_producer_test")
  implicit val mat = ActorMaterializer()
  
  val producerSettings: ProducerSettings[Array[Byte], String] = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")
  
  val done = Source(1 to 10)
    .map(_.toString)
    .map { elem =>
      new ProducerRecord[Array[Byte], String]("topic1", elem)
    }
    .runWith(Producer.plainSink(producerSettings))
  
//  Consumer.committableSource(consumerSettings, Subscriptions.topics("topic1"))
//    .map { msg =>
//      println(s"topic1 -> topic2: $msg")
//      ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
//        "topic2",
//        msg.record.value
//      ), msg.committableOffset)
//    }
//    .runWith(Producer.commitableSink(producerSettings))
  
}
