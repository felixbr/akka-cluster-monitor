package webapp

import org.scalajs.dom.raw.Node
import org.scalajs.dom.raw.HTMLDocument

object MainPage {
  val page = new MainPage(scalatags.JsDom)

  private var headElems = Seq.empty[Node]
  private var bodyElems = Seq.empty[Node]

  def appendTo(document: HTMLDocument): Unit = {
    val components = page.renderComponents()

    removeFrom(document)

    headElems = components.head.map(document.head.appendChild)
    bodyElems = components.body.map(document.body.appendChild)
  }

  def removeFrom(document: HTMLDocument): Unit = {
    headElems.foreach(document.removeChild)
    bodyElems.foreach(document.removeChild)
  }
}

class MainPage[Builder, Output <: FragT, FragT](val bundle: scalatags.generic.Bundle[Builder, Output, FragT])
  extends CSSMixin with RenderMixin[Output] {

  import bundle.all._

  val navbar = {
    div(cls := "navbar navbar-inverse navbar-fixed-top")(
      div(cls := "container-fluid")(
        div(cls := "navbar-header")(
          button(`type` := "button", cls := "navbar-toggle collapsed", "data-toggle".attr := "collapse", "data-target".attr := "#navbar", "aria-expanded".attr := "false")(
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
  }

  val sidebar = {
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
  }

  val content = {
    div(cls := "col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main")(
      h1(cls := "page-header")("Cluster Nodes"),
      table(cls := "table table-hover")(
        thead(
          tr(
            th(cls := "")("Address"),
            th(cls := "")("Status")
          )
        ),
        tbody(
          tr(
            td("Address1"), td("Up")
          ),
          tr(cls := "success")(
            td("Address2"), td("Joining")
          ),
          tr(
            td("Address3"), td("Up")
          ),
          tr(
            td("Address4"), td("Up")
          )
        )
      )
    )
  }

  override def renderComponents(): RenderedTags = RenderedTags(
    Seq(
      header.render
    ),
    Seq(
      navbar.render,
      sidebar.render,
      content.render
    )
  )
}
