package webapp

import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.all._
import webapp.components.{Sidebar, Navbar}
import webapp.pages._

object routing {
  trait Page
  case object Main extends Page
  case object ClusterOverview extends Page

  val routeConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    (emptyRule
      | staticRedirect(root)                             ~> redirectToPage(Main)(Redirect.Replace)
      | staticRoute("#clusteroverview", ClusterOverview) ~> renderR(ctl => ClusterOverviewPage.component(ctl))
      | staticRoute("#main", Main)                       ~> renderR(ctl => MainPage.component(ctl))
    ).notFound(redirectToPath("#main")(Redirect.Replace))
  }.renderWith(layout)

  def layout(c: RouterCtl[Page], r: Resolution[Page]) = {
    div(
      Navbar.component(),
      Sidebar.component(),
      div(id:="app", cls:="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main")(
        r.render()
      )
    )
  }
}
