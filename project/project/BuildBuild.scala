import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  val mleGroup = "com.github.malliina"

  override lazy val settings = super.settings ++ sbtPlugins ++ Seq(
    scalaVersion := "2.10.6",
    resolvers += ivyResolver("malliina bintray sbt", url("https://dl.bintray.com/malliina/sbt-plugins/")),
    libraryDependencies ++= Seq(mleGroup %% "jenkins-control" % "0.0.5")
  )

  def ivyResolver(name: String, repoUrl: sbt.URL) =
    Resolver.url(name, repoUrl)(Resolver.ivyStylePatterns)

  def sbtPlugins = Seq(
//    "me.lessis" % "bintray-sbt" % "0.3.0"
  ) map addSbtPlugin

  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))
}
