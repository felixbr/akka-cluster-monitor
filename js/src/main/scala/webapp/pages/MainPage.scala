package webapp.pages

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.router2.RouterCtl
import webapp.routing.{ClusterOverview, Page}

object MainPage {
  import japgolly.scalajs.react.vdom.all._

  val component = ReactComponentB[RouterCtl[Page]]("start page").render { ctl =>
    div(
      h2("Landing Page"),
      ctl.link(ClusterOverview)("overview")
    )
  }.build
}