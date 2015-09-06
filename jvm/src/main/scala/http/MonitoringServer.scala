package http

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.http.scaladsl.marshalling.{PredefinedToEntityMarshallers, Marshal}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.ConfigFactory
import http.websockets.handlerFlow
import config.websocket
import webapp.pages.IndexHtmlPage

import scala.io.StdIn.readLine

object MonitoringServer extends {
  val port = 2600
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
    .withFallback(ConfigFactory.load())

  implicit val system = ActorSystem("ClusterSystem", config)
  implicit val mat = ActorMaterializer()

  // use 'text/html' instead of 'text/plain' for serializing Strings
  implicit val marshaller = PredefinedToEntityMarshallers.stringMarshaller(MediaTypes.`text/html`)

  val route =
    pathEndOrSingleSlash {
      complete {
        IndexHtmlPage.asHtmlString
      }
    } ~
    path(websocket.route) {
      get {
        handleWebsocketMessages(handlerFlow)
      }
    } ~
    get {
      getFromResourceDirectory("web")
    }

  def main(args: Array[String]): Unit = {
    implicit val ec = system.dispatcher

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    val cluster = Cluster(system)

    readLine("Press ENTER to quit...\n\n")
    println("Shutting down...\n\n")

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete { _ =>
        cluster.registerOnMemberRemoved {
          system.terminate()
        }
        cluster.leave(cluster.selfAddress)
      }
  }
}
