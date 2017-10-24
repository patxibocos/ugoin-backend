import com.typesafe.sbt.packager.docker.ExecCmd

name := """ugoin-backend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  jdbc,
  ws,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.mockito" % "mockito-core" % "2.10.0" % Test,
  "com.typesafe.play" %% "play-slick" % "3.0.2",
  "mysql" % "mysql-connector-java" % "5.1.44",
  "com.h2database" % "h2" % "1.4.196" % Test,
  "io.swagger" %% "swagger-play2" % "1.6.0",
  "org.webjars" % "swagger-ui" % "3.2.0"
)

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)

dockerCommands ++= Seq(
  ExecCmd("RUN", "chmod", "+x", "/opt/docker/bin/ugoin-backend")
)

dockerEntrypoint ++= Seq(
  "-Dconfig.resource=prod.conf",
  "-Dlogger.resource=prod-logger.xml"
)