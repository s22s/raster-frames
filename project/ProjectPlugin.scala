import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._

import _root_.bintray.BintrayPlugin.autoImport._
import com.servicerocket.sbt.release.git.flow.Steps._
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.site.SitePlugin.autoImport._
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin.autoImport._
import de.heikoseeberger.sbtheader.CommentStyleMapping
import de.heikoseeberger.sbtheader.HeaderKey.headers
import de.heikoseeberger.sbtheader.license.Apache2_0
import tut.TutPlugin.autoImport._
import GhpagesPlugin.autoImport._
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport._

/**
 * @author sfitch
 * @since 8/20/17
 */
object ProjectPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  val versions = Map(
    "geotrellis" -> "1.1.1",
    "spark" -> "2.1.0"
  )

  private def geotrellis(module: String) =
    "org.locationtech.geotrellis" %% s"geotrellis-$module" % versions("geotrellis")
  private def spark(module: String) =
    "org.apache.spark" %% s"spark-$module" % versions("spark")

  override def projectSettings = Seq(
    organization := "io.astraea",
    startYear := Some(2017),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    headers := CommentStyleMapping.createFrom(Apache2_0, "2017", "Astraea, Inc."),
    scalaVersion := "2.11.11",
    scalacOptions ++= Seq("-feature", "-deprecation"),

    resolvers ++= Seq(
      "locationtech-releases" at "https://repo.locationtech.org/content/groups/releases"
    ),
    libraryDependencies ++= Seq(
      spark("core") % Provided,
      spark("mllib") % Provided,
      spark("mllib") % Tut,
      spark("sql") % Provided,
      spark("sql") % Tut,
      geotrellis("spark") % Provided,
      geotrellis("spark") % Tut,
      geotrellis("raster") % Provided,
      geotrellis("raster") % Tut,
      geotrellis("spark-testkit") % Test excludeAll(
        ExclusionRule(organization = "org.scalastic"),
        ExclusionRule(organization = "org.scalatest")
      ),
      "org.scalatest" %% "scalatest" % "3.0.3" % Test
    ),
    publishArtifact in Test := false,
    fork in Test := true,
    javaOptions in Test := Seq("-Xmx2G"),
    parallelExecution in Test := false,
    fork in tut := true
//    javaOptions in tut := Seq(
//      "-Dlog4j.configuration=file:src/test/resources/log4j.properties"
//    )
  )

  object autoImport {
    def releaseSettings: Seq[Def.Setting[_]] = {
      val buildSite: (State) ⇒ State = releaseStepTask(makeSite)
      val releaseArtifacts = releaseStepTask(bintrayRelease)
      Seq(
        bintrayOrganization := Some("s22s"),
        bintrayReleaseOnPublish in ThisBuild := false,
        publishArtifact in (Compile, packageDoc) := false,
        releaseProcess := Seq[ReleaseStep](
          checkSnapshotDependencies,
          checkGitFlowExists,
          inquireVersions,
          runTest,
          gitFlowReleaseStart,
          setReleaseVersion,
          buildSite,
          commitReleaseVersion,
          publishArtifacts,
          releaseArtifacts,
          gitFlowReleaseFinish,
          setNextVersion,
          commitNextVersion
        )
      )
    }

    def docSettings: Seq[Def.Setting[_]] = Seq(
      paradoxProperties in Paradox ++= Map(
        "github.base_url" -> s"https://github.com/s22s/raster-frames/tree/${version.value}"
      ),
      sourceDirectory in Paradox := tutTargetDirectory.value,
      makeSite := makeSite.dependsOn(tut).value,
      git.remoteRepo := "git@github.com:s22s/raster-frames.git",
      ghpagesNoJekyll := true
//      tutTargetDirectory := baseDirectory.value
    )
  }
}
