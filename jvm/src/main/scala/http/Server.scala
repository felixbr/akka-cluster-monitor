package http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import http.websockets.handlerFlow
import config.websocket

import scala.io.StdIn.readLine

object Server extends {
  implicit val system = ActorSystem("ClusterSystem")
  implicit val mat = ActorMaterializer()

  val route =
    pathEndOrSingleSlash {
      getFromResource("web/index.html")
    } ~
    path(websocket.route) {
      get {
        handleWebsocketMessages(handlerFlow)
      }
    } ~
    get {
      getFromResourceDirectory("web")
    } ~
    path("js") {
      get {
        getFromDirectory("client/target/scala-2.11")
      }
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
