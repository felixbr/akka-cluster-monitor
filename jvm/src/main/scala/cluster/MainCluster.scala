package cluster

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import util.LocalHostMixin

import scala.io.StdIn.readLine

object MainCluster extends LocalHostMixin {

  def startup(ports: Seq[Int]): Seq[Cluster] = {
    ports.map { port =>
      val system = ActorSystem("ClusterSystem", configWithPort(port))
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")

      Cluster(system)
    }
  }

  def shutdown(clusterNode: Cluster): Unit = {
    clusterNode.leave(clusterNode.selfAddress)
    clusterNode.system.terminate()
  }

  def main(args: Array[String]): Unit = {
    val nodes = startup(List(2551, 2552))

    readLine("Press ENTER to quit...\n\n")
    println("Shutting down...\n\n")

    nodes.foreach(shutdown)
  }
}
