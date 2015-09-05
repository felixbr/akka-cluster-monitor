package stream

import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import stream.PushMessageActor.PushMessage

import scala.annotation.tailrec

class PushMessageActor extends ActorPublisher[PushMessage] {

  val cluster = Cluster(context.system)

  var buf = Vector.empty[PushMessage]

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsSnapshot, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) =>
      self ! PushMessage(s"Member is now up: ${member.address}")

    case UnreachableMember(member) =>
      self ! PushMessage(s"Member detected as unreachable: $member")

    case MemberRemoved(member, previousStatus) =>
      self ! PushMessage(s"Member is removed: ${member.address} after $previousStatus")

    case CurrentClusterState(members, _, _, _, _) =>
      self ! PushMessage(s"Current Members: ${members.fold("  ")(_ + "\n  " + _)}")

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
      self ! PushMessage(other.toString)
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

object PushMessageActor {
  case class PushMessage(content: String)

  def props = Props[PushMessageActor]
}
