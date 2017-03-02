enablePlugins(JavaAppPackaging)

packageName in Universal := (name in Global).value + '-' + (version in Global).value
executableScriptName := (name in Global).value
makeBatScript := None // Disable generating of .bat script
scriptClasspath := Seq("*")

/*
  More settings can come here. Please refer to http://www.scala-sbt.org/sbt-native-packager/formats/universal.html
 */
// copy customised confuration files
mappings in Universal ++= {
  val defaultConf = (resourceDirectory in Compile).value / "reference.conf"
  Seq(defaultConf -> "conf/application.conf.example")
}

// add system properties to bash script
bashScriptExtraDefines ++= Seq("""addJava "-Dconfig.file=${app_home}/../conf/application.conf"""")