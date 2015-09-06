package webapp

import domain._
import japgolly.scalajs.react.React
import org.scalajs.dom
import org.scalajs.dom._
import upickle.default._
import webapp.pages.MainPage
import webapp.components.table.memberTable

import scala.scalajs.js.JSApp

object Webapp extends JSApp {
  def main(): Unit = {
    MainPage.appendTo(dom.document)

    val tableNode = dom.document.getElementById("cluster-member-table")
    initWebsocket(handleServerMessage(tableNode))
  }

  def initWebsocket(processMessage: String => Unit): Unit = {
    import config.websocket._

    val ws = new dom.WebSocket(s"ws://$host:$port/$route")
    ws.onopen = (e: Event) => println(s"ws event: ${e.`type`}")
    ws.onmessage = (m: MessageEvent) => processMessage(m.data.toString)
    ws.onerror = (e: ErrorEvent) => println(s"ws error: ${e.`type`} -> ${e.message}")
    ws.onclose = (e: CloseEvent) => {
      println(s"ws event: ${e.`type`}")

      // try reconnect
      dom.setTimeout(() => initWebsocket(processMessage), 1000)
    }
  }

  def handleServerMessage(tableNode: Element)(data: String): Unit = {
    println("ws event: received data")
    val json = read[ClusterMembers](data)
    json match {
      case ClusterMembers(members) =>
        println(s"rendering data: $members")
        React.render(memberTable(members), tableNode)

      case otherData =>
        println(s"ignoreing data: $otherData")
    }
  }
}
