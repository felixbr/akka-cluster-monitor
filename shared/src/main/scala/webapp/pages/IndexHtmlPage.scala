package webapp.pages

import webapp.CSSMixin

object IndexHtmlPage {
  val page = new IndexHtmlPage()(scalatags.Text)

  def asHtmlString: String = "<!DOCTYPE html>" + page.content.render
}

class IndexHtmlPage[Builder, Output <: FragT, FragT](implicit val bundle: scalatags.generic.Bundle[Builder, Output, FragT])
  extends CSSMixin[Builder, Output, FragT] {
  import bundle.all._
  import bundle.tags2.title

  val content = {
    html(lang:="en")(
      head(
        meta(charset:="UTF-8"),
        title("Akka Cluster Monitor"),

        script(src:="//code.jquery.com/jquery-1.11.3.min.js"),
        script(src:="//code.jquery.com/jquery-migrate-1.2.1.min.js"),
        link(rel:="stylesheet", href:="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"),
        link(rel:="stylesheet", href:="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css"),
        script(src:="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"),
        cssHeader
      ),
      body(
        script(`type`:="text/javascript", src:="js/akka-cluster-monitor-dashboard-fastopt.js"),
        script(`type`:="text/javascript")(
          "webapp.Webapp().main()"
        )
      )
    )
  }

}
