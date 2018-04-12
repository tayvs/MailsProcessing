name := "CSV2Mongo"

version := "0.1"

scalaVersion := "2.12.5"
scalacOptions ++= Seq(
//  "-Ylog:jvm",
  "-target:jvm-1.8",

//  "-optimise",
//  "-opt:l:method",
//  "-Ybackend-parallelism:4",
  "-Ycache-plugin-class-loader:last-modified",
  "-Ycache-macro-class-loader:last-modified"
)

//(scalacOptions in Build).map ::= Seq(
//    "-optimise",
//    "-opt:l:method"
//)

libraryDependencies ++= {
  
  val akkaV = "2.5.9"
  
  Seq(
    //reactiveMongo - MongoDB
    "org.reactivemongo"      %% "reactivemongo"           % "0.13.0",
    "org.reactivemongo"      %% "reactivemongo-akkastream"% "0.13.0",
    //Avro serialisator
//    "org.apache.avro"        % "avro"                     % "1.8.3",
    "com.sksamuel.avro4s"    %% "avro4s-core"             % "1.8.3",
    //akka
    "com.typesafe.akka"      %% "akka-actor"              % akkaV,
    "com.typesafe.akka"      %% "akka-remote"             % akkaV,
    "com.typesafe.akka"      %% "akka-stream"             % akkaV,
    //akka persistence and journaling core
    "com.typesafe.akka"      %% "akka-persistence"        % akkaV,
    "org.iq80.leveldb"            % "leveldb"             % "0.7",
    "org.fusesource.leveldbjni"   % "leveldbjni-all"      % "1.8",
    //kafka-akka streams
    "com.typesafe.akka"      %% "akka-stream-kafka"       % "0.19",
    
//    "com.typesafe.akka"      %% "akka-http"               % "10.0.11",
//    "com.typesafe.akka"      %% "akka-http-spray-json"    % "10.0.11",
    "org.scalaj"             %% "scalaj-http"             % "2.3.0",
    //CSV reader
    "com.github.tototoshi"   %% "scala-csv"               % "1.3.5",
    //java mailing and network api
    "javax.mail"             % "mail"                     % "1.4.7",
    //logging
    "ch.qos.logback"         % "logback-classic"          % "1.2.3",
    "com.typesafe.akka"      %% "akka-slf4j"              % akkaV
  )
  
//  enablePlugins(JmhPlugin)

}