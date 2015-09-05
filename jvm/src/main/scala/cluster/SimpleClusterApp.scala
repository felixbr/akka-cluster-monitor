package cluster

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object SimpleClusterApp {

  def startup(ports: Seq[Int]) = {
    ports.foreach { port =>
      val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
        .withFallback(ConfigFactory.load())

      val system = ActorSystem("ClusterSystem", config)
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")
    }
  }

//  def main(args: Array[String]): Unit = {
//    startup(List(2552, 2553))
//  }
}
