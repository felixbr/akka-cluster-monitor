import akka.actor.{Address, ActorSystem}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object NodeJoin extends App {

  val port = 2555
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
    .withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)

  val cluster = Cluster(system)

  val seedNodes = config.getStringList("akka.cluster.seed-nodes").asScala.map(stringToAddress).toList

//  println(seedNodes)

  Future {
    Thread.sleep(5.seconds.toMillis)
    println(cluster.state.members)
//    println(cluster.state.members.toList.map(m => m.address.host.getOrElse(m.address.hostPort)))
  }

  def stringToAddress(url: String): Address = {
    val pattern = """(.+)://(.+)@(.+):(.+)""".r

    url match {
      case pattern(protocol, name, matchedHost, matchedPort) =>
        new Address(protocol, name, matchedHost, matchedPort.toInt)

      case _ =>
        Address("akka.tcp", "ClusterSystem")
    }
  }
}
