package http

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.PredefinedToEntityMarshallers
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import config.websocket
import http.websockets.handlerFlow
import util.LocalHostMixin
import webapp.pages.IndexHtmlPage

import scala.io.StdIn.readLine

object MonitoringServer extends LocalHostMixin {
  implicit val system = ActorSystem("ClusterSystem", configWithPort(2600))
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
    import config.websocket.{host, port}
    implicit val ec = system.dispatcher

    val bindingFuture = Http().bindAndHandle(route, host, port)
    val cluster = Cluster(system)

    readLine("Press ENTER to quit...\n\n")
    println("Shutting down...\n\n")

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete { _ =>
        cluster.leave(cluster.selfAddress)
        system.terminate()
      }
  }
}
