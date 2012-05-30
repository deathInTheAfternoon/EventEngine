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
