package webapp

import org.scalajs.dom.html

trait RenderMixin[Output] {
  type HeadComponents = Seq[html.Link]
  type BodyComponents = Seq[Output]

  case class RenderedTags(head: HeadComponents, body: BodyComponents)

  def renderComponents(): RenderedTags
}
