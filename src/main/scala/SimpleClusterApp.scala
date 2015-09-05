import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

object SimpleClusterApp extends App {

  def startup(ports: Seq[Int]) = {
    ports.foreach { port =>
      val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
        .withFallback(ConfigFactory.load())

      val system = ActorSystem("ClusterSystem", config)
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")
    }
  }

  startup(List(2552, 2553))

}
