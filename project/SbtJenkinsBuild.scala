import bintray.BintrayKeys.{bintray => bintrayConf, bintrayOrganization, bintrayRepository}
import sbt.Keys._
import sbt._

/**
  * A scala build file template.
  */
object SbtJenkinsBuild extends Build {
  lazy val sbtJenkins = Project("sbt-jenkins-control", file("."))
    .enablePlugins(bintray.BintrayPlugin)
    .settings(projectSettings: _*)

  val malliinaOrg = "com.malliina"

  lazy val projectSettings = Seq(
    version := "0.2.3",
    scalaVersion := "2.10.6",
    organization := malliinaOrg,
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      malliinaOrg %% "jenkins-control" % "0.3.3"
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
