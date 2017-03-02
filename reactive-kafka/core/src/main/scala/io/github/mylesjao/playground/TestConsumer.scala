package io.github.mylesjao.playground

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset, CommittableOffsetBatch}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.pattern.AskTimeoutException
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.{ActorAttributes, ActorMaterializer, Supervision}
import com.github.ghik.silencer.silent
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.serialization.{LongDeserializer, StringDeserializer}

object TestConsumer {

  private def transformKafkaMessage(msg: CommittableMessage[java.lang.Long, String]) = {
    (msg.record.value(), Option(msg.committableOffset))
  }

  private def process(msg: String, takeTime: Long)(implicit ec: ExecutionContext): Future[Unit] = {
    import scala.concurrent.blocking
    Future {
      blocking {
        TimeUnit.MILLISECONDS.sleep(takeTime)
        println(s"[${Thread.currentThread().getName}] - process msg: $msg")
      }
    }
  }

  @silent
  private def batchSeed(committableOffset: Option[CommittableOffset]): CommittableOffsetBatch = committableOffset match {
    case Some(offset) => CommittableOffsetBatch.empty.updated(offset)
    case None => CommittableOffsetBatch.empty
  }

  @silent
  private def createGraph(implicit system: ActorSystem,
                          ec: ExecutionContext,
                          materializer: ActorMaterializer) = {
    import scala.concurrent.duration._

    val appConfig = ConfigFactory.load()
    val topic = appConfig.getString("app.data-sub.topic")
    val processTimeMs = appConfig.getLong("app.data-sub.process-time-ms")
    val partitionNum = appConfig.getInt("app.data-sub.partition-num")

    val consumerSettings = ConsumerSettings(system, new LongDeserializer, new StringDeserializer)

    Consumer.committablePartitionedSource(consumerSettings, Subscriptions.topics(topic))
      .map { case (_, source) =>
        source
          .map(transformKafkaMessage)
          .mapAsync(1) { case (value, offset) =>
            process(value, processTimeMs).map( _ => offset)
          }
          .withAttributes(ActorAttributes.supervisionStrategy(decider("map async: process message")))
          .groupedWithin(50, 5.seconds)
          .map(group => group.foldLeft(CommittableOffsetBatch.empty) { (batch, elem) => elem.fold(batch)(batch.updated) })
          .mapAsync(100){ offsetBatch =>
            offsetBatch.commitScaladsl().andThen {
              case Success(_) => println(s"offsets:[${offsetBatch.offsets()}] committed")
              case Failure(e) => println(s"offsets:[${offsetBatch.offsets()}] commit failed !! due to ${e.getClass}")
            }
          }
          .withAttributes(ActorAttributes.supervisionStrategy(decider("map async: commit kafka")))
          .toMat(Sink.ignore)(Keep.right)
          .run()
      }
      .mapAsyncUnordered(partitionNum)(identity)
      .withAttributes(ActorAttributes.supervisionStrategy(decider("map async unordered: identity")))
      .to(Sink.ignore)
  }

  @silent
  def run(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("TestConsumer")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val runningGraph = createGraph.run()

    sys.addShutdownHook {
      println("Shutdown hook called !!")

      val shutdown = for {
        _ <- runningGraph.stop()
        shutdown <- runningGraph.shutdown()
      } yield shutdown

      println("wait for consumer shutdown...")
      Try(Await.result(shutdown, Duration.Inf)) match {
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

    println("TestConsumer run...")
  }

  private def decider(place: String): Supervision.Decider = {
    case e: KafkaException =>
      println(s"get kafka error in [$place].. stream stopped")
      e.printStackTrace()
      Supervision.Stop
    case e: AskTimeoutException =>
      println(s"[${LocalDateTime.now()}]: get actor AskTimeoutException in [$place]. stream restart")
      e.printStackTrace()
      Supervision.Restart
    case e =>
      println(s"encounter unexpected error in [$place]. stream stopped")
      e.printStackTrace()
      Supervision.Stop
  }
}
