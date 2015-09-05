package cluster

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._


class SimpleClusterListener extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      println(s"Member is now up: ${member.address}")

    case UnreachableMember(member) =>
      println(s"Member detected as unreachable: $member")

    case MemberRemoved(member, previousStatus) =>
      println(s"Member is removed: ${member.address} after $previousStatus")

    case _: MemberEvent => // ignore
  }
}
