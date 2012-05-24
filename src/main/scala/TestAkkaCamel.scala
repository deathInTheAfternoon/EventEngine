/**
 * Created under license Apache 2.0
 * User: nthakur
 * Date: 14/05/12
 * Time: 07:19
 *
 */

import akka.actor.{Actor, ActorSystem, Props}
import akka.camel._
import javax.naming.Context
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.{Message, Exchange, Processor, CamelContext}
import org.drools.command.{Command, CommandFactory}
import org.drools.runtime.StatefulKnowledgeSession
import org.drools.{KnowledgeBaseFactory, KnowledgeBase}
import org.drools.builder.{ResourceType, KnowledgeBuilderFactory, KnowledgeBuilder}
import org.drools.grid.GridNode
import org.drools.grid.impl.GridImpl
import org.drools.io.ResourceFactory
import org.springframework.context.support.ClassPathXmlApplicationContext

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
          //todo: Dangerous, manual conversion to String, for now
          var m: Command[_] = CommandFactory.newInsert(exchange.getIn.getBody(classOf[String]))
          var far: Command[_] = CommandFactory.newFireAllRules
          var cmds: java.util.List[Command[_]] = new java.util.ArrayList[Command[_]]
          cmds.add(m)
          cmds.add(far)

          exchange.getIn.setBody(CommandFactory.newBatchExecution(cmds))
        }
      }).to("drools:node/ksession?action=execute")
    }
  })

  // todo: we have to kSession.dispose. Also previous experiences suggest we may have to use StatefulSession instead?
  def createKnowledgeSession(): StatefulKnowledgeSession = {
    var kBuilder: KnowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder()
    kBuilder.add(ResourceFactory.newClassPathResource("KBExpertise.drl"), ResourceType.DRL)
    var kBase: KnowledgeBase = KnowledgeBaseFactory.newKnowledgeBase()
    kBase.addKnowledgePackages(kBuilder.getKnowledgePackages)
    var kSession: StatefulKnowledgeSession = kBase.newStatefulKnowledgeSession()
    kSession.addEventListener(DroolsEventLogger)

    kSession
  }

  def configureGridContext(ksession: StatefulKnowledgeSession) = {
    var grid = new GridImpl()
    var gridNode: GridNode = grid.createGridNode("node")
    // the default registry is a JNDIRegistry - from looking at akka-camel code.
    var r1: org.apache.camel.impl.PropertyPlaceholderDelegateRegistry = CamelExtension(sys).context.getRegistry() .asInstanceOf[org.apache.camel.impl.PropertyPlaceholderDelegateRegistry]
    var r2: org.apache.camel.impl.JndiRegistry = r1.getRegistry.asInstanceOf[org.apache.camel.impl.JndiRegistry]

    gridNode.set("ksession", ksession)
    // JNDI is a wrapper around JNDIContext.
    r2.bind("node", gridNode)
  }

}

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
