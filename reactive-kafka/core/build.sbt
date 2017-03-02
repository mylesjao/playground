name := s"${(name in Global).value}-core"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka"      % "0.13"
)
