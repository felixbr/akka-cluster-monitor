package webapp.components

import domain.Member
import japgolly.scalajs.react.ReactComponentB


object table {
  import japgolly.scalajs.react.vdom.prefix_<^._

  private implicit class RichMember(member: Member) {
    def hasStatus(status: String): Boolean = member.status == status
  }

  val memberRow = ReactComponentB[Member]("data row").render { member =>
    <.tr(
      ^.classSet(
        "success" -> member.hasStatus("Joining"),
        "error" -> member.hasStatus("Down")
      ),
      <.td(member.address.protocol),
      <.td(member.address.host),
      <.td(member.address.port),
      <.td(member.status)
    )
  }.build

  val memberTable = ReactComponentB[Set[Member]]("table of members").render { members =>
    <.table(
      ^.cls := "table table-hover",
      <.thead(
        <.tr(
          <.th("Protocol"),
          <.th("Host"),
          <.th("Port"),
          <.th("Status")
        )
      ),
      <.tbody(
        members.map(m => memberRow(m))
      )
    )
  }.build
}
