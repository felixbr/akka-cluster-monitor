package webapp.components

import japgolly.scalajs.react.ReactComponentB

object Sidebar {
  import japgolly.scalajs.react.vdom.all._

  val component = ReactComponentB[Unit]("sidebar").render { _ =>
    div(cls := "container-fluid")(
      div(cls := "row")(
        div(cls := "col-sm-3 col-md-2 sidebar")(
          ul(cls := "nav nav-sidebar")(
            li(cls := "active")(a(href := "#")("Overview ", span(cls := "sr-only")("(current)"))),
            li(a(href := "#")("Reports")),
            li(a(href := "#")("Analytics")),
            li(a(href := "#")("Export"))
          ),
          ul(cls := "nav nav-sidebar")(
            li(a(href := "#")("Nav Item")),
            li(a(href := "#")("Nav Item"))
          )
        )
      )
    )
  }.buildU
}
