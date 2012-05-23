/**
 * Created under license Apache 2.0
 * User: nthakur
 * Date: 14/05/12
 * Time: 07:19
 *
 */

import akka.actor.{Actor, ActorSystem, Props}
import akka.camel._
import akka.util.Duration
import java.rmi.registry.Registry
import javax.naming.Context
import org.apache.camel.builder.RouteBuilder
//import org.apache.camel.component.amqp.AMQPComponent
//import org.apache.camel.component.jms.JmsConfiguration
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.util.jndi.JndiContext
import org.apache.camel.{Exchange, Processor, CamelContext}
//import org.apache.qpid.client.AMQConnectionFactory
import org.drools.builder.{ResourceType, KnowledgeBuilderFactory, KnowledgeBuilder}
import org.drools.event.rule.{BeforeActivationFiredEvent, DefaultAgendaEventListener}
import org.drools.grid.GridNode
import org.drools.grid.impl.GridImpl
import org.drools.io.ResourceFactory
import org.drools.runtime.StatelessKnowledgeSession
import org.drools.{KnowledgeBaseFactory, KnowledgeBase}
import org.springframework.context.support.ClassPathXmlApplicationContext
import akka.util.duration._

object TestAkkaCamel extends App {
  startSpring()

  def startSpring(){
    var ctx = new ClassPathXmlApplicationContext("springContext.xml");
    val ta = ctx.getBean("tommy").asInstanceOf[Person];
    print(ta.name)
  }

  class Orders extends Actor with Producer with Oneway {
    //todo: untested
    def endpointUri = "spring-amqp:businessX:outgoingQ:outgoingQ?type=direct"  // todo: I've had to manually create the Q - why?
  }

  class MyConsumer extends Consumer {
    def endpointUri = "spring-amqp:SimulationX:tempQ?type=fanout"
    def receive = {
      case msg: CamelMessage =>
        println("Simulator Message %s" format msg.bodyAs[String])
    }
  }

  System.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.camel.util.jndi.CamelInitialContextFactory");

  val sys = ActorSystem("snapcracklepop")
  val orders = sys.actorOf(Props[Orders])
  val myconsumer = sys.actorOf(Props[MyConsumer])

  var ctxt = configureGridContext(createKnowledgeSession())

  // Manually setting up routes without using akka-camel for the moment.
  val context: CamelContext = CamelExtension(sys).context
  context.addRoutes(new RouteBuilder() {
    def configure() {
      from("spring-amqp:businessX:businessQ:businessQ?type=direct&autodelete=false")
        .process(new Processor {
        def process(exchange: Exchange) {
          println("Manually Configured Route Received: %s" format(exchange.getIn.getBody))
        }
      }).to("drools:node/ksession?action=insertMessage")
    }
  })

  // todo: we have to kSession.dispose. Also previous experiences suggest we may have to use StatefulSession instead?
  def createKnowledgeSession(): StatelessKnowledgeSession = {
    var kBuilder: KnowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder()
    kBuilder.add(ResourceFactory.newClassPathResource("drools_rules.drl"), ResourceType.DRL)
    var kBase: KnowledgeBase = KnowledgeBaseFactory.newKnowledgeBase()
    kBase.addKnowledgePackages(kBuilder.getKnowledgePackages)
    var kSession: StatelessKnowledgeSession = kBase.newStatelessKnowledgeSession()
    kSession.addEventListener(DroolsEventLogger)
    kSession
  }

  def configureGridContext(ksession: StatelessKnowledgeSession) = {
    var grid = new GridImpl()
    //grid.addService(org.drools.grid.service.directory.WhitePages.class, new WhitePagesImpl())
    var gridNode: GridNode = grid.createGridNode("node")
    // the default registry is a JNDIRegistry - from looking at akka-camel code.
    var r1: org.apache.camel.impl.PropertyPlaceholderDelegateRegistry = CamelExtension(sys).context.getRegistry() .asInstanceOf[org.apache.camel.impl.PropertyPlaceholderDelegateRegistry]
    var r2: org.apache.camel.impl.JndiRegistry = r1.getRegistry.asInstanceOf[org.apache.camel.impl.JndiRegistry]

    gridNode.set("ksession", ksession)
    // JNDI is a wrapper around JNDIContext.
    r2.bind("node", gridNode)
  }

}

