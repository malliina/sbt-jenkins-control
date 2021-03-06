import bintray.BintrayKeys.{bintray => bintrayConf, bintrayOrganization, bintrayRepository}
import sbt.Keys._
import sbt._

object SbtJenkinsBuild {
  lazy val sbtJenkins = Project("sbt-jenkins-control", file("."))
    .enablePlugins(bintray.BintrayPlugin)
    .settings(projectSettings: _*)

  val malliinaOrg = "com.malliina"

  lazy val projectSettings = Seq(
    version := "0.3.2-SNAPSHOT",
    scalaVersion := "2.10.6",
    organization := malliinaOrg,
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      malliinaOrg %% "jenkins-control" % "0.4.0"
    ),
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      Resolver.bintrayRepo("malliina", "maven")
    ),
    bintrayOrganization in bintrayConf := None,
    bintrayRepository in bintrayConf := "sbt-plugins",
    publishMavenStyle := false,
    licenses +=("MIT", url("http://opensource.org/licenses/MIT"))
  )
}
