package http

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.http.scaladsl._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.stream._
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory
import domain.Implicits._
import domain._
import stream.PushMessageActor.PushMessage
import stream._
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn.readLine

object websockets {
//  val config = ConfigFactory.parseString("akka.cluster.seed-nodes = []")  // don't cluster fow now
//    .withFallback(ConfigFactory.load())

  val config = ConfigFactory.load()

  implicit lazy val system = ActorSystem("ClusterSystem", config)
  implicit lazy val materializer = ActorMaterializer()

  case class WSStreamEvent[T](e: T)

  private def setupRequestHandler(websocketService: Flow[Message, Message, Any]): HttpRequest => HttpResponse = {
    case req @ HttpRequest(GET, Uri.Path("/ws"), _, _, _) =>
      req.header[UpgradeToWebsocket] match {
        case Some(upgrade) => upgrade.handleMessages(websocketService)
        case None          => HttpResponse(400, entity = "Not a valid websocket request!")
      }

    case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")
  }


  def handlerFlow(implicit system: ActorSystem): Flow[Message, Message, Any] = {
    val cluster = Cluster(system)

    Flow() { implicit b =>
      import akka.stream.scaladsl.FlowGraph.Implicits._

      val pushSource = b.add(Source.actorPublisher[PushMessage](PushMessageActor.props))

      val merge = b.add(Merge[WSStreamEvent[_]](2))

      val msgToEvent = b.add(Flow[Message].collect[WSStreamEvent[String]] { case TextMessage.Strict(txt) => WSStreamEvent(txt) })
      val pushMsgToString = b.add(Flow[PushMessage].collect[WSStreamEvent[PushMessage]] { case p @ PushMessage(_) => WSStreamEvent(p) })
      val stringToMsg = b.add(Flow[String].map[Message](TextMessage(_)))

      val commandDispatch = b.add(Flow[WSStreamEvent[_]].collect[String] {
        case WSStreamEvent("nodes") =>
          write(CurrentMembers(cluster.state.members))

        case WSStreamEvent(PushMessage(msg)) =>
          msg

        case _ =>
          write(Error("unknown command"))
      })

      pushSource ~> pushMsgToString ~> merge ~> commandDispatch ~> stringToMsg
                        msgToEvent ~> merge

      (msgToEvent.inlet, stringToMsg.outlet)
    }
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

//  def main(args: Array[String]): Unit = {
//    startListening(handlerFlow)
//  }
}
