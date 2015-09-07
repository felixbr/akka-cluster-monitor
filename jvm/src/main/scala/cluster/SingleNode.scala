package cluster

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import util.LocalHostMixin

import scala.io.StdIn.readLine

object SingleNode extends LocalHostMixin {

  def initNode() = {
    val system = ActorSystem("ClusterSystem", configWithPort(2553))
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
