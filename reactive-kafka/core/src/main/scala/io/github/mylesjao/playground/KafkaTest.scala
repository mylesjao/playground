package io.github.mylesjao.playground

object KafkaTest extends App {

  args.headOption match {
    case None =>
      println("""Please assign role of producer or consumer !!""")
      sys.exit(0)
    case Some(role) if role == "producer" =>
      TestProducer.run(args)
    case Some(role) if role == "consumer" =>
      TestConsumer.run(args)
    case Some(r) =>
      println(s"""Unknown role: $r. It should be one of [producer, consumer]""")
      sys.exit(0)
  }

}
