name := "CSV2Mongo"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  
  val akkaV = "2.4.17"
  
  Seq(
    "org.reactivemongo"      %% "reactivemongo"           % "0.12.2",
    "org.reactivemongo"      %% "reactivemongo-akkastream"% "0.12.3",
  
    "com.typesafe.akka"      %% "akka-actor"              % akkaV,
    "com.typesafe.akka"      %% "akka-remote"             % akkaV,
    "com.typesafe.akka"      %% "akka-stream"             % akkaV,
//    "com.typesafe.akka"      %% "akka-http"               % "10.0.11",
//    "com.typesafe.akka"      %% "akka-http-spray-json"    % "10.0.11",
    "org.scalaj"             %% "scalaj-http"             % "2.3.0",
    
    "com.github.tototoshi"   %% "scala-csv"               % "1.3.5",
  
    "javax.mail"             % "mail"                     % "1.4.7",
  
    "ch.qos.logback"         % "logback-classic"          % "1.2.3"
  )
}