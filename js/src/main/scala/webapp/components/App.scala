package webapp.components

import japgolly.scalajs.react.ReactComponentB

object App {
  import japgolly.scalajs.react.vdom.all._

  val mainPage = ReactComponentB[Unit]("main page").render { _ =>
    div(id:="app",
      Navbar.component(),
      Sidebar.component()
    )
  }.buildU
}
