package net.nthakur.model

import org.scala_tools.time.Imports._

/**
 * Created under license Apache 2.0
 * User: nthakur
 * Date: 28/05/12
 * Time: 17:05
 *
 */

case class EventHeader(eventType: String, instanceId: String, source: String,
                       occurrenceTime: org.joda.time.DateTime = new org.joda.time.DateTime(DateTimeZone.UTC),
                       detectionTime: org.joda.time.DateTime = new org.joda.time.DateTime(DateTimeZone.UTC),
                       annotation: String = "None.", chronon: Char = 's',
                       isComposite: Boolean = false, certainty: Double = 1.0)

case class Payload(contents: String)

// a JSON String.
case class DomainEvent(header: EventHeader, payload: Payload)


// ----------------------------------------------------------------
// DOMAIN MODEL - without these the rules will (silently) fail to compile.

case class Someone(name:String, age:Int)

case class Car(someone:Someone, model:String, year:Int, color:Color)

case class Color(name:String)

object Color {
  val red = Color("red")
  val blue = Color("blue")
  val green = Color("green")
  val black = Color("black")
}

case class Address(street:String, town:String, country:String)

case class Home(someone:Someone, address:Option[Address])

case class InformationRequest(someone:Someone, message:String)
