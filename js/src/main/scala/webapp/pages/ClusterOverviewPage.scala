package webapp.pages

import domain.Member
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import webapp.Api.ClusterMembersUpdate
import webapp.{WSClose, WSError}
import webapp.components.ClusterMemberTable
import webapp.events.PubSub
import webapp.routing.Page

case class State(members: Set[Member])

object ClusterOverviewPage {
  import japgolly.scalajs.react.vdom.all._

  val component = ReactComponentB[RouterCtl[Page]]("cluster members overview page")
    .initialState(State(Set.empty))
    .backend(new Backend(_))
    .render { (_, S, B) =>
      div(cls := "col-sm-9 col-md-10 main")(
        h1(cls := "page-header")("Cluster Nodes"),
        ClusterMemberTable.memberTable(ClusterMemberTable.Props(S.members))
      )
    }.build

}

class Backend(t: BackendScope[RouterCtl[Page], State]) {
  PubSub.subscribe {
    case ClusterMembersUpdate(members) => t.modState(_ => State(members))
    case WSClose => t.modState(_ => State(Set.empty))
    case WSError => t.modState(_ => State(Set.empty))
  }
}
