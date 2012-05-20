/**
 * Created under license Apache 2.0
 * User: nthakur
 * Date: 14/05/12
 * Time: 07:19
 *
 */

import akka.actor.{Actor, ActorSystem, Props}
import akka.camel._
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.amqp.AMQPComponent
import org.apache.camel.component.jms.JmsConfiguration
import org.apache.camel.{Exchange, Processor, CamelContext}
import org.apache.qpid.client.AMQConnectionFactory
import org.drools.builder.{ResourceType, KnowledgeBuilderFactory, KnowledgeBuilder}
import org.drools.io.ResourceFactory
import org.drools.runtime.StatelessKnowledgeSession
import org.drools.{KnowledgeBaseFactory, KnowledgeBase}

object TestAkkaCamel extends App {

  //FYI: orders ! <order amount="100" currency="PLN" itemId="12345"/>
  class Orders extends Actor with Producer with Oneway {
    //def endpointUri = "file:C:/test.txt"
    def endpointUri = "amqp:BURL:direct://businessX/outgoingQ/outgoingQ"  // todo: I've had to manually create the Q - why?
  }

  class BasketListener extends Consumer {
    def endpointUri = "amqp:BURL:direct://businessX//businessQ"

    def receive = {
      case msg: CamelMessage => println("Business Message %s" format msg.bodyAs[String])
    }
  }

  class MyConsumer extends Consumer {
    def endpointUri = "amqp:BURL:fanout://SimulationX//"

    def receive = {
      case msg: CamelMessage => println("Simulator Message %s" format msg.bodyAs[String])
    }
  }

  createKnowledgeSession()

  val sys = ActorSystem("snapcracklepop")
  val orders = sys.actorOf(Props[Orders])
  //val myconsumer = sys.actorOf(Props[MyConsumer])
  //val basketListener = sys.actorOf(Props[BasketListener])

  val connectionFactory = new AMQConnectionFactory
  // All the following are required...otherwise messages just wont appear.
  connectionFactory.setHost("localhost")
  connectionFactory.setPort(5672)
  connectionFactory.setDefaultPassword("guest")
  connectionFactory.setDefaultUsername("guest")
  connectionFactory.setVirtualPath("/")

  CamelExtension(sys).context.addComponent("amqp", new AMQPComponent(new JmsConfiguration(connectionFactory)))

  // Manually setting up routes without using akka-camel for the moment.
  val context: CamelContext = CamelExtension(sys).context
  context.addRoutes(new RouteBuilder() {
    def configure() {
      from("amqp:BURL:direct://businessX//businessQ")
        .process(new Processor {
        def process(exchange: Exchange) {
          println("Manually Configured Route Received: %s" format(exchange.getIn.getBody))
        }
      }).to(orders)
    }
  })

  def createKnowledgeSession(): StatelessKnowledgeSession = {
    var kBuilder: KnowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder()
    kBuilder.add(ResourceFactory.newClassPathResource("test.drl"), ResourceType.DRL)
    var kBase: KnowledgeBase = KnowledgeBaseFactory.newKnowledgeBase()
    kBase.addKnowledgePackages(kBuilder.getKnowledgePackages)
    var kSession: StatelessKnowledgeSession = kBase.newStatelessKnowledgeSession()
    kSession
  }
}

