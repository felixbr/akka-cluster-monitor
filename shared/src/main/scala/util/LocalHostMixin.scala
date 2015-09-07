package util

import java.net.InetAddress
import com.typesafe.config.ConfigFactory

trait LocalHostMixin {
  val hostname = InetAddress.getLocalHost.getHostAddress

  def configWithPort(port: Int) = {
    val configStr = s"""akka.remote.netty.tcp.hostname = "$hostname"
                       |akka.remote.netty.tcp.port = $port
                       |akka.cluster.seed-nodes = [
                       |  "akka.tcp://ClusterSystem@$hostname:2551",
                       |  "akka.tcp://ClusterSystem@$hostname:2552"
                       |]""".stripMargin
    println(s"$configStr\n")

    ConfigFactory.parseString(configStr).withFallback(ConfigFactory.load())
  }
}
