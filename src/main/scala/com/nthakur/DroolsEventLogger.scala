package com.nthakur

import org.drools.definition.rule.Rule
import org.drools.event.rule.{BeforeActivationFiredEvent, DefaultAgendaEventListener}

/**
 * Created under license Apache 2.0
 * User: nthakur
 * Date: 21/05/12
 * Time: 14:53
 *
 */

object DroolsEventLogger extends DefaultAgendaEventListener {
  override def beforeActivationFired(event: BeforeActivationFiredEvent) {
    val rule: Rule = event.getActivation.getRule
    Console.println(event.getClass.getSimpleName)
  }
}
