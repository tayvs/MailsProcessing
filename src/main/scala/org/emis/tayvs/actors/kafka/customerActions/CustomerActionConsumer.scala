package org.emis.tayvs.actors.kafka.customerActions

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, Deserializer, StringDeserializer}

object CustomerActionConsumer extends App {
  
  implicit val system: ActorSystem = ActorSystem(
    "kafka_customerActions_consumer",
    ConfigFactory.load.getConfig("kafka_customerActions_consumer")
      .withFallback(ConfigFactory.load.getConfig("akka.kafka.consumer")))
  implicit val mat: ActorMaterializer = ActorMaterializer()
  
  val customerActionsTopic = "customerAction"
  
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  
  Consumer
    .committableSource(
      consumerSettings,
//      Subscriptions.assignmentWithOffset(new TopicPartition(customerActionsTopic, 0) -> 0L)
      Subscriptions.topics(customerActionsTopic)
    )
    .map { record =>
      println(record.record.value)
      record.committableOffset.commitScaladsl()
    }
    .runWith(Sink.ignore)
  
}
