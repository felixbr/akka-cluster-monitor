package http

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.http.scaladsl.model.ws._
import akka.stream.scaladsl._
import domain.Implicits._
import domain._
import stream.PushMessageActor.PushMessage
import stream._
import upickle.default._

object websockets {
  case class WSStreamEvent[T](e: T)

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
}
