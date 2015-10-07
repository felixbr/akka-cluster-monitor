package webapp.components

import japgolly.scalajs.react.ReactComponentB

object Navbar {
  import japgolly.scalajs.react.vdom.all._

  val component = ReactComponentB[Unit]("navbar").render { _ =>
    div(cls := "navbar navbar-inverse navbar-fixed-top")(
      div(cls := "container-fluid")(
        div(cls := "navbar-header")(
          button(`type` := "button", cls := "navbar-toggle collapsed", "data-toggle".reactAttr := "collapse",
            "data-target".reactAttr := "#navbar", "aria-expanded".reactAttr := "false")(

            span(cls := "sr-only")("Toggle Navigation"),
            span(cls := "icon-bar")(),
            span(cls := "icon-bar")(),
            span(cls := "icon-bar")()
          ),
          a(cls := "navbar-brand", href := "#")("Akka Cluster Monitor")
        ),
        div(id := "navbar", cls := "navbar-collapse collapse")(
          ul(cls := "nav navbar-nav navbar-right")(
            li(a(href := "#")("Dashboard")),
            li(a(href := "#")("Settings")),
            li(a(href := "#")("Profile")),
            li(a(href := "#")("Help"))
          ),
          form(cls := "navbar-form navbar-right")(
            input(`type` := "text", cls := "form-control", placeholder := "Search...")
          )
        )
      )
    )
  }.buildU
}
