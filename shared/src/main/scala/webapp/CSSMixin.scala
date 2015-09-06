package webapp

trait CSSMixin[Builder, Output <: FragT, FragT] {
  val pageName = this.getClass.getSimpleName

  def cssHeader(implicit bundle: scalatags.generic.Bundle[Builder, Output, FragT]) = {
    import bundle.all._

    {
      link(rel:="stylesheet", href:=s"css/$pageName.css")
    }
  }
}
