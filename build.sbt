import de.heikoseeberger.sbtheader.CommentStyleMapping
import de.heikoseeberger.sbtheader.license.Apache2_0
import sbt._
import sbt.Keys._
import sbtrelease.Vcs

scalaVersion := "2.11.8"

name := "raster-frames"

organization := "io.astraea"

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

headers := CommentStyleMapping.createFrom(Apache2_0, "2017", "Astraea, Inc.")

resolvers ++= Seq(
  "locationtech-releases" at "https://repo.locationtech.org/content/groups/releases"
)

def geotrellis(module: String) = "org.locationtech.geotrellis" %% s"geotrellis-$module" % "1.1.1"

def spark(module: String) = "org.apache.spark" %% s"spark-$module" % "2.1.0"

libraryDependencies ++= Seq(
  spark("core") % Provided,
  spark("mllib") % Provided,
  spark("sql") % Provided,
  spark("mllib") % "tut",
  spark("sql") % "tut",
  geotrellis("spark") % Provided,
  geotrellis("spark") % "tut",
  geotrellis("raster") % "tut",
  geotrellis("spark-testkit") % Test,
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
)

fork in Test := true

scalacOptions ++= Seq("-feature", "-deprecation")

bintrayOrganization := Some("s22s")

bintrayReleaseOnPublish in ThisBuild := false

publishArtifact in (Compile, packageDoc) := false

enablePlugins(TutPlugin)

tutTargetDirectory := baseDirectory.value

import ReleaseTransformations._
import com.servicerocket.sbt.release.git.flow.Steps._

lazy val runTut = releaseStepTask(tut)
lazy val commitTut = ReleaseStep((st: State) ⇒ {
  val extracted = Project.extract(st)
  val vcs = extracted.get(releaseVcs).get
  vcs.add("README.md").!
  val status = vcs.status.!!.trim
  if (status.nonEmpty) {
    vcs.commit("Updated README.md") ! st.log
  }
  st
})

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  checkGitFlowExists,
  inquireVersions,
  runTest,
  gitFlowReleaseStart,
  setReleaseVersion,
  runTut,
  commitTut,
  commitReleaseVersion,
  publishArtifacts,
  gitFlowReleaseFinish,
  pushMaster,
  setNextVersion,
  commitNextVersion
)


