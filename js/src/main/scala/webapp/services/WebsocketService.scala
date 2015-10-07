package webapp.services

import org.scalajs.dom
import org.scalajs.dom._
import webapp.Api._
import webapp.pickling._


object WebsocketService {

  def initWebsocket(
    handleMessage: ServerMessage => Unit,
    handleError: ErrorEvent => Unit,
    handleClose: CloseEvent => Unit
  ): Unit = {
    import config.websocket._

    val ws = new dom.WebSocket(s"ws://$host:$port/$route")

    ws.onopen = (e: Event) => println(s"ws event: ${e.`type`}")

    ws.onmessage = (m: MessageEvent) => {
      val deserialized = deserializeServerMessage(m.data.toString)
      println(s"ws message: ${deserialized.getClass.toString}")
      handleMessage(deserialized)
    }

    ws.onerror = (e: ErrorEvent) => {
      println(s"ws error: ${e.`type`} -> ${e.message}")
      handleError(e)
    }

    ws.onclose = (e: CloseEvent) => {
      println(s"ws event: ${e.`type`}")
      handleClose(e)

      // try reconnect
      dom.setTimeout(() => initWebsocket(handleMessage, handleError, handleClose), 1000)
    }
  }
}
