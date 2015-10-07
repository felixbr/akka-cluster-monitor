package webapp

import japgolly.scalajs.react.extra.router2._
import org.scalajs.dom
import org.scalajs.dom.CloseEvent
import org.scalajs.dom.ErrorEvent
import services.WebsocketService
import webapp.Api.ServerMessage
import webapp.events.PubSub
import webapp.routing.routeConfig

import scala.scalajs.js.JSApp

case object WSError
case object WSClose

object Webapp extends JSApp {
  type State = String

  val router = Router(BaseUrl(dom.window.location.href.takeWhile(_ != '#')), routeConfig)

  def main(): Unit = {
    render()


    WebsocketService.initWebsocket(
      (m: ServerMessage) => PubSub.publish(m),
      (_: ErrorEvent) => PubSub.publish(WSError),
      (_: CloseEvent) => PubSub.publish(WSClose)
    )
  }

  def render(): Unit = {
    router() render dom.document.body
  }
}
