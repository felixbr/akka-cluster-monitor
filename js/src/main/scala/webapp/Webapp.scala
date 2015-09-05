package webapp

import org.scalajs.dom
import org.scalajs.dom.{CloseEvent, Event, ErrorEvent, MessageEvent}

import scala.annotation.tailrec
import scala.scalajs.js.JSApp
import scala.scalajs.runtime.UndefinedBehaviorError

object Webapp extends JSApp {
  def main(): Unit = {
    initWebsocket(t => t)
  }

  def initWebsocket(processMessage: String => String): Unit = {
    val hostPort = "127.0.0.1:8080"

    val ws = new dom.WebSocket(s"ws://$hostPort/ws")
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
