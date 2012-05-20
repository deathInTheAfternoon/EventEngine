name := "Basket Generator"

version := "0.1"

scalaVersion := "2.9.1"

// We're using this as a Maven repo - but brining in typesafe components from snapshot repo below.
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.4"

libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.6.4"

libraryDependencies += "org.apache.camel" % "camel-core" % "2.9.2"

// This is a JMS client that translates to AMQP v0.91 so SHOULD work with Rabbit.
libraryDependencies += "org.apache.camel" % "camel-amqp" % "2.9.2"

// JMS Api for Camel JMS Component
libraryDependencies += "javax.jms" % "jms" % "1.1"

libraryDependencies += "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0"

//Drools engine
libraryDependencies += "org.drools" % "knowledge-api" % "5.4.0.Final"

libraryDependencies += "com.sun.xml.bind" % "jaxb-xjc" % "2.2.5"

libraryDependencies += "org.drools" % "drools-compiler" % "5.4.0.Final"

libraryDependencies += "org.drools" % "drools-core" % "5.4.0.Final"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

//this also transitively brings in akka-actor of the same version.
libraryDependencies += "com.typesafe.akka" % "akka-camel" % "2.1-20120513-092052"

//we need these JVM options so QPid client can talk to RabbitMQ
javaOptions in run += "-Dqpid.amqp.version=0-91"

javaOptions in run += "-Dqpid.dest_syntax=BURL"

javaOptions in run += "-DSTRICT_AMQP=true"