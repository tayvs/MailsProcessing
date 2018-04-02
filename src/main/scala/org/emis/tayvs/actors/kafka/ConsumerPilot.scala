package org.emis.tayvs.actors.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import scala.concurrent.Future
import java.util.concurrent.atomic.AtomicLong
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

object ConsumerPilot extends App {
  
  implicit val system = ActorSystem("kafka_producer_test")
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  
  class DB {
    
    private val offset = new AtomicLong()
    
    def save(record: ConsumerRecord[Array[Byte], String]): Future[Done] = {
      println(s"DB.save: ${record.value}")
      offset.set(record.offset)
      Future.successful(Done)
    }
    
    def loadOffset(): Future[Long] =
      Future.successful(offset.get)
    
    def update(data: String): Future[Done] = {
      println(s"DB.update: $data")
      Future.successful(Done)
    }
  }
  
  class Rocket {
    def launch(destination: String): Future[Done] = {
      println(s"Rocket launched to $destination")
      Future.successful(Done)
    }
  }
  
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  
//  val db = new DB
//  db.loadOffset().foreach { fromOffset =>
//    val partition = 0
//    val subscription = Subscriptions.assignmentWithOffset(
//      new TopicPartition("topic1", partition) -> fromOffset
//    )
//
//    val done =
//      Consumer.plainSource(consumerSettings, subscription)
//        .mapAsync(1)(db.save)
//        .runWith(Sink.ignore)
//  }

//  val db = new DB
//  db.loadOffset().foreach { fromLongTime =>
//    val partition = 0
//    val subscription = Subscriptions.assignmentOffsetsForTimes(
//      new TopicPartition("topic1", partition) -> fromLongTime
//    )
//    val done =
//      Consumer.plainSource(consumerSettings, subscription)
//        .mapAsync(1)(db.save)
//        .runWith(Sink.ignore)
//  }

  Consumer.committableSource(consumerSettings, Subscriptions.topics("topic1"))
    .map(el => el.record.value())
    .runForeach(println)

//  val partition = 0
//  val subscription = Subscriptions.assignmentWithOffset(
//    new TopicPartition("topic1", partition) -> 0L
//  )
//  val done =
//    Consumer.plainSource(consumerSettings, subscription)
////      .mapAsync(1)(db.save)
//      .runWith(Sink.foreach(println))

}
