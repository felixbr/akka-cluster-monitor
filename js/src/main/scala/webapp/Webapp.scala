package webapp

import org.scalajs.dom
import org.scalajs.dom.{CloseEvent, ErrorEvent, Event, MessageEvent}

import scala.scalajs.js.JSApp

object Webapp extends JSApp {
  def main(): Unit = {
    initWebsocket(t => t)
    MainPage.appendTo(dom.document)
  }

  def initWebsocket(processMessage: String => String): Unit = {
    import config.websocket._

    val ws = new dom.WebSocket(s"ws://$host:$port/$route")
    ws.onopen = (e: Event) => println(s"ws event: ${e.`type`}")
    ws.onmessage = (m: MessageEvent) => {
      println(m.data)
      processMessage(m.data.toString)
    }
    ws.onerror = (e: ErrorEvent) => println(s"ws error: ${e.`type`} -> ${e.message}")
    ws.onclose = (e: CloseEvent) => {
      println(s"ws event: ${e.`type`}")

      // try reconnect
      dom.setTimeout(() => initWebsocket(processMessage), 1000)
    }
  }
}
