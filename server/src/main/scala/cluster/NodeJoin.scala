package cluster

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

object NodeJoin extends App {
  val port = 2555
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
    .withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)

  val cluster = Cluster(system)

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
