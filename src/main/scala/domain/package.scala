package domain {

import scala.collection.immutable.SortedSet
import akka.actor.Address

import aliases._

import scala.language.implicitConversions

object aliases {
  type Role = String
  type Status = String
}

case class Member(address: Address, status: Status, roles: Set[Role])

object Member {
  implicit def fromClusterMember(m: akka.cluster.Member): Member = Member(m.address, m.status.toString, m.roles)
}


case class MemberJoined(member: Member)

case class MemberLeft(member: Member, previousStatus: Status)

case class CurrentMembers(members: Set[Member])

case class Error(error: String)

object Implicits {
  implicit def membersFromClusterMembers(ms: SortedSet[akka.cluster.Member]): Set[Member] = ms.map(Member.fromClusterMember)
}

}

