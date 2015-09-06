package cluster

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

import scala.io.StdIn.readLine

object MainCluster {

  def startup(ports: Seq[Int]): Seq[Cluster] = {
    ports.map { port =>
      val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
        .withFallback(ConfigFactory.load())

      val system = ActorSystem("ClusterSystem", config)
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")

      Cluster(system)
    }
  }

  def shutdown(clusterNode: Cluster): Unit = {
    clusterNode.registerOnMemberRemoved {
      clusterNode.system.terminate()
    }
    clusterNode.leave(clusterNode.selfAddress)
  }

  def main(args: Array[String]): Unit = {
    val nodes = startup(List(2551, 2552))

    readLine("Press ENTER to quit...\n\n")
    println("Shutting down...\n\n")

    nodes.foreach(shutdown)
  }
}
