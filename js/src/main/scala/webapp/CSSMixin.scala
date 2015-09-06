package webapp

import scalatags.JsDom.all._

trait CSSMixin {
  val pageName = this.getClass.getSimpleName

  val header = {
    link(rel:="stylesheet", href:=s"css/$pageName.css")
  }
}
