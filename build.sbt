import com.typesafe.sbt.packager.docker.ExecCmd

name := """ugoin-backend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  jdbc,
  ws,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.mockito" % "mockito-core" % "2.13.0" % Test,
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "mysql" % "mysql-connector-java" % "5.1.45",
  "com.h2database" % "h2" % "1.4.196" % Test
)

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)

dockerCommands ++= Seq(
  ExecCmd("RUN", "chmod", "+x", "/opt/docker/bin/ugoin-backend")
)

dockerEntrypoint ++= Seq(
  "-Dconfig.resource=prod.conf",
  "-Dlogger.resource=prod-logger.xml"
)