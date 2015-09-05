package http

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.scaladsl.FlowGraph.Implicits._
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn.readLine

object websockets {
  val config = ConfigFactory.parseString("akka.cluster.seed-nodes = []")  // don't cluster fow now
    .withFallback(ConfigFactory.load())

  implicit val system = ActorSystem("WebsocketSystem", config)
  implicit val materializer = ActorMaterializer()

  def processTextFlow(processMessage: String => String): Flow[Message, Message, Any] = Flow[Message].collect {
    case TextMessage.Strict(txt) => TextMessage(processMessage(txt))
  }

  private def setupRequestHandler(websocketService: Flow[Message, Message, Any]): HttpRequest => HttpResponse = {
    case req @ HttpRequest(GET, Uri.Path("/ws"), _, _, _) =>
      req.header[UpgradeToWebsocket] match {
        case Some(upgrade) => upgrade.handleMessages(websocketService)
        case None          => HttpResponse(400, entity = "Not a valid websocket request!")
      }

    case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")
  }

  def startListening(websocketProcessingFlow: Flow[Message, Message, Any], host: String = "localhost", port: Int = 8080): Unit = {
    val bindingFuture =
      Http().bindAndHandleSync(setupRequestHandler(websocketProcessingFlow), interface = host, port = port)

    println("Server online at http://localhost:8080/\nPress RETURN to stop...")
    readLine()

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def main(args: Array[String]): Unit = {
    startListening(processTextFlow(t => t))
  }
}