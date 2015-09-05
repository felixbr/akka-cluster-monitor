package stream

import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import domain._
import stream.PushMessageActor._
import upickle.default._

import scala.annotation.tailrec

object PushMessageActor {
  case class PushMessage(msg: String)

  def props = Props[PushMessageActor]
}

class PushMessageActor extends ActorPublisher[PushMessage] {

  val cluster = Cluster(context.system)

  var buf = Vector.empty[PushMessage]

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsSnapshot, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) =>
      self ! PushMessage(write(MemberJoined(member)))

    case UnreachableMember(member) =>

    case MemberRemoved(member, prevStatus) =>
      self ! PushMessage(write(MemberLeft(member, prevStatus.toString)))

    case CurrentClusterState(members, unreachable, _, _, _) =>
      self ! PushMessage(write(CurrentMembers(members.map(Member.fromClusterMember))))

    case p @ PushMessage(msg) =>
      if (buf.isEmpty && totalDemand > 0) {
        onNext(p)
      } else {
        buf :+= p
        deliverBuf()
      }

    case Request(_) =>
      deliverBuf()

    case other =>
  }

  @tailrec private def deliverBuf(): Unit = {
    if (totalDemand > 0) {
      if (totalDemand < Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use.foreach(onNext)

      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use.foreach(onNext)
        deliverBuf()
      }
    }
  }

}