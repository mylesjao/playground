package io.github.mylesjao.playground

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{LongSerializer, StringSerializer}

object TestProducer {

  def run(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("TestProducer")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val appConfig = ConfigFactory.load()
    val topic = appConfig.getString("app.data-pub.topic")
    val size = appConfig.getLong("app.data-pub.size")
    val numOfKeys = appConfig.getInt("app.data-pub.partition-num")

    val producerSettings = ProducerSettings(system, new LongSerializer, new StringSerializer)

    val producer = producerSettings.createKafkaProducer()

    println(s"data size to produce: $size")
    val finished = Source(1L to size)
      .map { n =>
        val key = n % numOfKeys
        new ProducerRecord[java.lang.Long, String](topic, key, s"$key-$n")
      }
      .runWith(Producer.plainSink(producerSettings, producer))

    println("wait for publish done...")
    Try(Await.result(finished, Duration.Inf)) match {
      case Success(_) => println("done...")
      case Failure(e) => e.printStackTrace()
    }

    Try(Await.result(system.terminate(), Duration.Inf)) match {
      case Success(_) =>
        println("actor system terminate terminated..")
      case Failure(e) =>
        println("actor system terminate failed")
        e.printStackTrace()
    }
  }
}
