package http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{PredefinedToEntityMarshallers, Marshal}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import http.websockets.handlerFlow
import config.websocket
import webapp.pages.IndexHtmlPage

import scala.io.StdIn.readLine

object Server extends {
  implicit val system = ActorSystem("ClusterSystem")
  implicit val mat = ActorMaterializer()

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
    readLine("Press ENTER to quit...\n\n")

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
