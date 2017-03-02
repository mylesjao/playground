import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.github.ghik.silencer.silent

@silent
object AkkaStreamTest extends App {

  implicit val system: ActorSystem = ActorSystem("AkkaStreamTest")

  val decider: Supervision.Decider = {
    case e: ArithmeticException =>
      println("restart...")
      //throw e
      Supervision.Stop
    case _: IllegalArgumentException => Supervision.Stop
  }

  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  val source = Source(Vector(1,2,3,0,4,5))
    .map { x =>
      println(s"process $x")
      100 / x
    }
  val result = source.runWith(Sink.fold(0)(_ + _))

  println("====HAHAHAH=====")



  Try(Await.result(result, Duration.Inf)) match {
    case Success(d) =>  println(d)
    case Failure(e) =>
      println("!!!!ERROR and system.exit(0) !!!")
      sys.exit()
  }

}
