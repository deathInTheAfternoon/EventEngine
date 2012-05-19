name := "Basket Generator"

version := "0.1"

scalaVersion := "2.9.1"

// We're using this as a Maven repo - but brining in typesafe components from snapshot repo below.
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.4"

libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.6.4"

libraryDependencies += "org.apache.camel" % "camel-core" % "2.9.2"

// Apparently this is built for Apache Qpid and doesn't work with RabbitMQ.
libraryDependencies += "org.apache.camel" % "camel-amqp" % "2.9.2"

libraryDependencies += "javax.jms" % "jms" % "1.1"

libraryDependencies += "com.rabbitmq" % "amqp-client" % "2.8.2"

libraryDependencies += "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

//this also transitively brings in akka-actor of the same version.
libraryDependencies += "com.typesafe.akka" % "akka-camel" % "2.1-20120513-092052"

//we need these JVM options so QPid client can talk to RabbitMQ
javaOptions in run += "-Dqpid.amqp.version=0-91"

javaOptions in run += "-Dqpid.dest_syntax=BURL"

javaOptions in run += "-DSTRICT_AMQP=true"