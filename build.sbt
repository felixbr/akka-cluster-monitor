lazy val commonSettings = Seq(
  version := "0.1",

  scalaVersion := "2.11.7",

  scalacOptions ++= commonScalacOptions
)

val akkaStableVersion = "2.4-M2"
val akkaExperimentalVersion = "1.0"

resolvers += "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven"

val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")
val webOutputDir = Def.settingKey[File]("directory for static files")

lazy val js2jvmSettings = Seq(fastOptJS, fullOptJS, packageJSDependencies).map { packageJSKey =>
  crossTarget.in(Compile, packageJSKey) := scalajsOutputDir.value
}

lazy val root = crossProject.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % "0.5.2",
      "com.lihaoyi" %%% "upickle" % "0.3.6"
    )
  ).jsSettings(
    name := "akka-cluster-monitor-dashboard",
    scalaJSStage in Global := FastOptStage,

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "com.github.japgolly.scalajs-react" %%% "core" % "0.9.2"
    ),

    jsDependencies += "org.webjars" % "react" % "0.12.2" / "react-with-addons.js" commonJSName "React",
    skip in packageJSDependencies := false,

    scalajsOutputDir := (classDirectory in Compile).value / "web" / "js",  // compiled js into js classDirectory

    fastOptJS in Compile := {
      val base = (fastOptJS in Compile).value
      IO.copyDirectory((classDirectory in Compile).value / "web", webOutputDir.value)  // copy all static files from js to jvm
      base
    }
  ).jvmSettings(
    name := "akka-cluster-monitor",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-testkit" % akkaStableVersion,

      "com.google.guava" % "guava" % "18.0",

      "de.heikoseeberger" %% "akka-http-upickle" % "1.1.0",

      "org.scalatest" %% "scalatest" % "2.1.6" % "test"
    ),
    webOutputDir in Global := (classDirectory in Compile).value / "web"
  )

lazy val client: Project = root.js
  .dependsOn(shared)
  .settings(js2jvmSettings: _*)
  .enablePlugins(ScalaJSPlugin)

lazy val server: Project = root.jvm
  .settings(commonSettings: _*)
  .settings(
    (resources in Compile) += (fastOptJS in (client, Compile)).value.data
  )
  .dependsOn(shared)

lazy val shared = project.in(file("shared"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-contrib" % akkaStableVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaStableVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaStableVersion,
      "com.typesafe.akka" %% "akka-stream-experimental" % akkaExperimentalVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaExperimentalVersion,
      "com.typesafe.akka" %% "akka-http-core-experimental" % akkaExperimentalVersion,

      "com.lihaoyi" %%% "scalatags" % "0.5.2"
    )
  )

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
//  "-Ywarn-value-discard",
  "-Xfuture"
)

// A configuration which is like 'compile' except it performs additional static analysis.
// Execute static analysis via `lint:compile`
val LintTarget = config("lint").extend(Compile)

addMainSourcesToLintTarget()

addSlowScalacSwitchesToLintTarget()

addWartRemoverToLintTarget()

removeWartRemoverFromCompileTarget()

addFoursquareLinterToLintTarget()

removeFoursquareLinterFromCompileTarget()

def addMainSourcesToLintTarget() = {
  inConfig(LintTarget) {
    // I posted http://stackoverflow.com/questions/27575140/ and got back the bit below as the magic necessary
    // to create a separate lint target which we can run slow static analysis on.
    Defaults.compileSettings ++ Seq(
      sources in LintTarget := {
        val lintSources = (sources in LintTarget).value
        lintSources ++ (sources in Compile).value
      }
    )
  }
}

def addSlowScalacSwitchesToLintTarget() = {
  inConfig(LintTarget) {
    // In addition to everything we normally do when we compile, we can add additional scalac switches which are
    // normally too time consuming to run.
    scalacOptions in LintTarget ++= Seq(
      // As it says on the tin, detects unused imports. This is too slow to always include in the build.
      "-Ywarn-unused-import",
      //This produces errors you don't want in development, but is useful.
      "-Ywarn-dead-code"
    )
  }
}

def addWartRemoverToLintTarget() = {
  import wartremover._
  // I didn't simply include WartRemove in the build all the time because it roughly tripled compile time.
  inConfig(LintTarget) {
    wartremoverErrors ++= Seq(
      // Ban inferring Any, Serializable, and Product because such inferrence usually indicates a code error.
      // Wart.Any,
      Wart.Serializable,
      Wart.Product,
      // Ban calling partial methods because they behave surprisingingly
      Wart.ListOps,
      Wart.OptionPartial,
      Wart.EitherProjectionPartial,
      // Ban applying Scala's implicit any2String because it usually indicates a code error.
      Wart.Any2StringAdd
    )
  }
}

def removeWartRemoverFromCompileTarget() = {
  // WartRemover's sbt plugin calls addCompilerPlugin which always adds directly to the Compile configuration.
  // The bit below removes all switches that could be passed to scalac about WartRemover during a non-lint compile.
  scalacOptions in Compile := (scalacOptions in Compile).value filterNot { switch =>
    switch.startsWith("-P:wartremover:") ||
      "^-Xplugin:.*/org[.]brianmckenna/.*wartremover.*[.]jar$".r.pattern.matcher(switch).find
  }
}

def addFoursquareLinterToLintTarget() = {
  Seq(
    resolvers += "Linter Repository" at "https://hairyfotr.github.io/linteRepo/releases",
    addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1.9"),
    // See https://github.com/HairyFotr/linter#list-of-implemented-checks for a list of checks that foursquare linter
    // implements
    // By default linter enables all checks.
    // I don't mind using match on boolean variables.
    scalacOptions in LintTarget += "-P:linter:disable:PreferIfToBooleanMatch"
  )
}

def removeFoursquareLinterFromCompileTarget() = {
  // We call addCompilerPlugin in project/plugins.sbt to add a depenency on the foursquare linter so that sbt magically
  // manages the JAR for us.  Unfortunately, addCompilerPlugin also adds a switch to scalacOptions in the Compile config
  // to load the plugin.
  // The bit below removes all switches that could be passed to scalac about Foursquare Linter during a non-lint compile.
  scalacOptions in Compile := (scalacOptions in Compile).value filterNot { switch =>
    switch.startsWith("-P:linter:") ||
      "^-Xplugin:.*/com[.]foursquare[.]lint/.*linter.*[.]jar$".r.pattern.matcher(switch).find
  }
}