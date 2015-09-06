package cluster

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

import scala.io.StdIn.readLine

object SingleNode {

  def initNode() = {
    val port = 2553
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
      .withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val cluster = Cluster(system)

    readLine("Press ENTER to quit...\n\n")
    println("Shutting down...\n\n")

    cluster.registerOnMemberRemoved {
      system.terminate()
    }
    cluster.leave(cluster.selfAddress)
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

  def main(args: Array[String]): Unit = {
    initNode()
  }
}
