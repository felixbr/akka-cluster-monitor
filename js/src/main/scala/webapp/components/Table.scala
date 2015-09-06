package webapp.components

import domain.Member
import japgolly.scalajs.react.ReactComponentB


object table {
  import japgolly.scalajs.react.vdom.prefix_<^._

  private implicit class RichMember(member: Member) {
    def hasStatus(status: String): Boolean = member.status == status
  }

  val memberRow = ReactComponentB[(Member, Int)]("data row").render { args =>
    val (member, idx) = args

    <.tr(
      ^.classSet(
        "success" -> member.hasStatus("Joining"),
        "warning" -> member.hasStatus("Unreachable"),
        "warning" -> member.hasStatus("Exiting"),
        "error" -> member.hasStatus("Down")
      ),
      <.td(idx),
      <.td(member.address.protocol),
      <.td(member.address.host),
      <.td(member.address.port),
      <.td(
        ^.classSet(
          "info" -> member.hasStatus("Up")
        ),
        member.status
      )
    )
  }.build

  val memberTable = ReactComponentB[Set[Member]]("table of members").render { members =>
    <.table(
      ^.cls := "table table-hover",
      <.thead(
        <.tr(
          <.th("#"),
          <.th("Protocol"),
          <.th("Host"),
          <.th("Port"),
          <.th("Status")
        )
      ),
      <.tbody(
        members.zipWithIndex.map { case (m: Member, idx: Int) => memberRow((m, idx + 1)) }
      )
    )
  }.build
}
