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
  lazy val tableNode = dom.document.getElementById("cluster-member-table")

  def main(): Unit = {
    MainPage.appendTo(dom.document)

    initWebsocket(handleServerMessage)
  }

  def initWebsocket(handleMessage: String => Unit): Unit = {
    import config.websocket._

    val ws = new dom.WebSocket(s"ws://$host:$port/$route")
    ws.onopen = (e: Event) => println(s"ws event: ${e.`type`}")
    ws.onmessage = (m: MessageEvent) => handleMessage(m.data.toString)
    ws.onerror = (e: ErrorEvent) => {
      println(s"ws error: ${e.`type`} -> ${e.message}")
      updateTable(Set.empty)
    }
    ws.onclose = (e: CloseEvent) => {
      println(s"ws event: ${e.`type`}")
      updateTable(Set.empty)

      // try reconnect
      dom.setTimeout(() => initWebsocket(handleMessage), 1000)
    }
  }

  def handleServerMessage(data: String): Unit = {
    println("ws event: received data")
    read[ClusterMembers](data) match {
      case ClusterMembers(members) =>
        updateTable(members)

      case otherData =>
        println(s"ignoring data: $otherData")
    }
  }

  def updateTable(members: Set[Member]) = {
    React.render(memberTable(members), tableNode)
  }
}
