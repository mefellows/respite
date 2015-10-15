package respite

import org.scalatra.sbt.ScalatraPlugin
import sbt.Keys._
import sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object RespiteBuild extends Build {
  val Organization = "au.com.onegeek"
  val Name = "Respite REST Framework"
  val Version = "0.3.5  "

  lazy val core = Project (
    "respite-core",
    file("respite-core"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := Dependencies.Versions.ScalaVersion
    )
  )

 lazy val examples = Project (
    "respite-examples",
    file("respite-examples"),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(

      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := Dependencies.Versions.ScalaVersion,
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    ),
 dependencies = Seq(core)
 )
}