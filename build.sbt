ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.12.15"

val flinkVersion = "1.14.5"
val flinkStreamingScala = "org.apache.flink" %% "flink-streaming-scala" % flinkVersion
val flinRuntimeWeb = "org.apache.flink" %% "flink-runtime-web" % flinkVersion
val flinkClients = "org.apache.flink" %% "flink-clients" % flinkVersion
val flinkKafka = "org.apache.flink" %% "flink-connector-kafka" % flinkVersion
val flinkTestUtils = "org.apache.flink" %% "flink-test-utils" % flinkVersion % Test



lazy val root = (project in file("."))
  .settings(
    name := "flink-tests",
    libraryDependencies ++= Seq(
      flinkStreamingScala,
      flinRuntimeWeb,
      flinkClients,
      flinkKafka,
      flinkTestUtils
    ),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test
  )
