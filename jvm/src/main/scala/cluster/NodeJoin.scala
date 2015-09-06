package cluster

import akka.actor.{ActorSystem, Address}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn.readLine

object NodeJoin {

  def initNode() = {
    val port = 2555
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
      .withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)

    readLine("Press ENTER to quit...\n\n")
    val _ = system.terminate()
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
