name := "cchain"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {

  val akkaVersion       = "2.5.6"
  val cassandraVersion  = "3.1.1"
  val sprayVersion      = "1.3.2"
  val scalazVersion     = "7.2.15"

  Seq(
    "com.typesafe.akka"       %% "akka-actor"               % akkaVersion,
    "com.typesafe.akka"       %% "akka-slf4j"               % akkaVersion,
    "org.scalaz"              %% "scalaz-core"              % scalazVersion,
    "com.datastax.cassandra"  % "cassandra-driver-core"     % cassandraVersion,
    "c3p0"                    % "c3p0"                      % "0.9.1.2",
    "org.slf4j"               % "slf4j-api"                 % "1.7.5",
    "ch.qos.logback"          % "logback-classic"           % "1.0.9",
    "io.spray"                %% "spray-can"                % sprayVersion,
    "io.spray"                %% "spray-routing"            % sprayVersion,
    "io.spray"                %% "spray-client"             % sprayVersion,
    "io.spray"                %% "spray-json"               % sprayVersion,
    "org.scalatest"           % "scalatest_2.11"            % "2.2.1"               % "test"
  )
}

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".RSA" => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith ".keys" => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith "logs" => MergeStrategy.discard

  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
