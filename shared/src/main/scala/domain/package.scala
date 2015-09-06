package domain

import scala.collection.immutable.SortedSet

import aliases._

import scala.language.implicitConversions

object aliases {
  type Role = String
  type Status = String
}

case class Member(address: Address, status: Status, roles: Set[Role]) {
  override def equals(obj: scala.Any): Boolean = address.equals(obj)

  override def hashCode(): Int = address.hashCode()
}

object Member {
  implicit def fromClusterMember(m: akka.cluster.Member): Member = Member(m.address, m.status.toString, m.roles)
}

case class Address(protocol: String, system: String, host: Option[String], port: Option[Int])

object Address {
  implicit def fromActorAddress(a: akka.actor.Address): Address = Address(a.protocol, a.system, a.host, a.port)
}

case class MemberJoined(member: Member)

case class MemberLeft(member: Member, previousStatus: Status)

case class ClusterMembers(members: Set[Member])

case class Error(error: String)

object Implicits {
  implicit def membersFromClusterMembers(ms: SortedSet[akka.cluster.Member]): Set[Member] = ms.map(Member.fromClusterMember)
}

