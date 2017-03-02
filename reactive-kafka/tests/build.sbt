name := s"${(name in Global).value}-tests"

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.4",
  "org.scalatest"  %% "scalatest"  % "3.0.1"
)
