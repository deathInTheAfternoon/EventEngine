/**
 * Created under license Apache 2.0
 * User: nthakur
 * Date: 14/05/12
 * Time: 07:19
 *
 */

import akka.actor.{Actor, ActorSystem, Props}
import akka.camel._
import org.apache.camel.component.amqp.AMQPComponent
import org.apache.camel.component.jms.JmsConfiguration
import org.apache.qpid.client.AMQConnectionFactory

object TestAkkaCamel extends App {

  class Orders extends Actor with Producer with Oneway {
    //def endpointUri = "file:C:/test.txt"
    def endpointUri = "amqp:BURL:direct://businessX/businessQ/businessQ"
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

  val sys = ActorSystem("snapcracklepop")
  //val orders = sys.actorOf(Props[Orders])
  //val myconsumer = sys.actorOf(Props[MyConsumer])
  val basketListener = sys.actorOf(Props[BasketListener])

  val connectionFactory = new AMQConnectionFactory
  // All the following are required...otherwise messages just wont appear.
  connectionFactory.setHost("localhost")
  connectionFactory.setPort(5672)
  connectionFactory.setDefaultPassword("guest")
  connectionFactory.setDefaultUsername("guest")
  connectionFactory.setVirtualPath("/")

  CamelExtension(sys).context.addComponent("amqp", new AMQPComponent(new JmsConfiguration(connectionFactory)))

  //orders ! <order amount="100" currency="PLN" itemId="12345"/>
}

