name in Global := "reactive-kafka-test"
organization in Global := "io.github.mylesjao.playground"
scalaVersion in Global := "2.11.8"

lazy val root = project.in(file("."))
  .settings(name := (name in Global).value)
  .settings(coverageSettings)
  .settings(releaseSettings)
  .aggregate(core, bench, tests)

lazy val core = project
  .settings(scalacOptions ++= commonScalacOptions)
  .settings(silencerSettings)
  .settings(wartRemoverSettings)

lazy val tests = project
  .dependsOn(core)

lazy val testsUat = project.in(file("tests-uat"))
  .dependsOn(core)

lazy val bench = project
  .settings(noPublishing)
  .dependsOn(core)

lazy val wartRemoverSettings = Seq(
  wartremoverWarnings ++= Warts.allBut(Wart.Var, Wart.Equals)
)

lazy val commonScalacOptions = Seq(
    "-deprecation"             // Emit warning and location for usages of deprecated APIs
  , "-encoding", "UTF-8"
  , "-feature"                 // Emit warning and location for usages of features that should be imported explicitly
  , "-unchecked"               // Enable additional warnings where generated code depends on assumptions
  , "-Xfatal-warnings"         // Fail the compilation if there are any warnings
  , "-Xfuture"                 // Turn on future language features
  , "-Xlint"                   // Enable specific warnings (see `scalac -Xlint:help`)
  , "-Yno-adapted-args"        // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver
  , "-Ywarn-dead-code"         // Warn when dead code is identified
  , "-Ywarn-inaccessible"      // Warn about inaccessible types in method signatures
  , "-Ywarn-infer-any"         // Warn when a type argument is inferred to be `Any`
  , "-Ywarn-nullary-override"  // Warn when non-nullary `def f()' overrides nullary `def f'
  , "-Ywarn-nullary-unit"      // Warn when nullary methods return Unit
  , "-Ywarn-numeric-widen"     // Warn when numerics are widened
  , "-Ywarn-unused"            // Warn when local and private vals, vars, defs, and types are unused
  , "-Ywarn-unused-import"     // Warn when imports are unused
  , "-Ywarn-value-discard"     // Warn when non-Unit expression results are unused
)

lazy val silencerSettings = Seq(
  libraryDependencies += "com.github.ghik" % "silencer-plugin" % "0.4",
  addCompilerPlugin("com.github.ghik" % "silencer-plugin" % "0.4")
)

lazy val coverageSettings = Seq(
  coverageEnabled := true,
  coverageFailOnMinimum := true,
  coverageMinimum := 80,
  coverageOutputCobertura := false,
  coverageOutputXML := true
)

lazy val releaseSettings = Seq(
  releaseProcess := {
    import ReleaseTransformations._

    Seq[ReleaseStep](
        checkSnapshotDependencies
      , inquireVersions
      , releaseStepCommand("validate")
      , setReleaseVersion
      , commitReleaseVersion
      , tagRelease
  //    , publishArtifacts          // publishing is done by build server (Jenkins)
      , setNextVersion
      , commitNextVersion
      , pushChanges
    )
  }
)

lazy val noPublishing = Seq(publish := {}, publishLocal := {})

resolvers in Global ++= Seq(
  Resolver.mavenLocal
)

addCommandAlias("validate", ";clean;coverage;test;coverageReport;coverageAggregate")
addCommandAlias("benchmark", ";clean;bench/jmh:compile;bench/jmh:run")
addCommandAlias("package", ";core/universal:packageZipTarball")
addCommandAlias("stage", ";core/stage")
addCommandAlias("build", ";validate;coverageOff;package")
addCommandAlias("uat", ";testsUat/test")
addCommandAlias("run", ";core/run")
addCommandAlias("updates", ";dependencyUpdates")

