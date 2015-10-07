package webapp

import domain.Member
import upickle.default._
import webapp.Api.ServerMessage

object Api {
  sealed trait ServerMessage
  case class ClusterMembersUpdate(members: Set[Member]) extends ServerMessage
}

object pickling {
  def serializeServerMessage(msg: ServerMessage): String = write[ServerMessage](msg)

  def deserializeServerMessage(data: String): ServerMessage = read[ServerMessage](data)
}
