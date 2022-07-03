ThisBuild / scalaVersion := "2.13.8"
ThisBuild / name := (server / name).value
name := (ThisBuild / name).value

lazy val commonSettings: Seq[Setting[_]] = Seq(
  version := {
    val Tag = "refs/tags/(.*)".r
    sys.env.get("CI_VERSION").collect { case Tag(tag) => tag }
      .getOrElse("0.0.1-SNAPSHOT")
  },

  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(
    publishArtifact := false
  )
  .aggregate(server)

val circeVersion = "0.14.1"
val http4sVersion = "0.23.11"

lazy val common = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "org.scodec" %%% "scodec-bits" % "1.1.27",
      "org.typelevel" %%% "cats-effect" % "3.2.0",
    )
  )

lazy val commonJvm = common.jvm
lazy val commonJs = common.js

lazy val frontend = project
  .enablePlugins(ScalaJSWebjarPlugin)
  .dependsOn(commonJs)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core-bundle-cats_effect" % "2.0.0-RC2",
      "com.github.japgolly.scalajs-react" %%% "extra" % "2.0.0-RC2",
      "org.scala-js" %%% "scalajs-dom" % "1.1.0",
    ),

    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
    },
    scalaJSUseMainModuleInitializer := true,
  )

lazy val server = project
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(commonJvm, frontend.webjar)
  .settings(commonSettings)
  .settings(
    name := "server-desktop",

    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-scalatags" % http4sVersion,
      "org.http4s" %% "http4s-client" % http4sVersion,
      "org.http4s" %% "http4s-jdk-http-client" % "0.7.0",
      "org.apache.commons" % "commons-imaging" % "1.0-alpha3",
    ),

    buildInfoKeys := Seq(
      "frontendAsset" -> (frontend / Compile / webjarMainResourceName).value,
      "frontendName" -> (frontend / normalizedName).value,
      "frontendVersion" -> (frontend / version).value,
    ),
  )
