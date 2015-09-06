package webapp

import scalatags.text.Builder

trait RenderMixin[Output] {
  type HeadComponents = Seq[Output]
  type BodyComponents = Seq[Output]

  case class RenderedTags(head: HeadComponents, body: BodyComponents)

  def renderComponents(): RenderedTags
}
