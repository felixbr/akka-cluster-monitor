package http

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn.readLine

object websockets {
  val config = ConfigFactory.parseString("akka.cluster.seed-nodes = []")  // don't cluster fow now
    .withFallback(ConfigFactory.load())

  implicit val system = ActorSystem("WebsocketSystem", config)
  implicit val materializer = ActorMaterializer()

  val websocketService = Flow[Message].mapConcat {
    case tm: TextMessage =>
      TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil

    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      Nil
  }

  val requestHandler: HttpRequest => HttpResponse = {
    case req @ HttpRequest(GET, Uri.Path("/ws"), _, _, _) =>
      req.header[UpgradeToWebsocket] match {
        case Some(upgrade) => upgrade.handleMessages(websocketService)
        case None          => HttpResponse(400, entity = "Not a valid websocket request!")
      }

    case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")
  }

  def startListening(host: String = "localhost", port: Int = 8080): Unit = {
    val bindingFuture =
      Http().bindAndHandleSync(requestHandler, interface = host, port = port)

    println("Server online at http://localhost:8080/\nPress RETURN to stop...")
    readLine()

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def main(args: Array[String]): Unit = {
    startListening()
  }
}