//import bintray.BintrayKeys.{bintray => bintrayConf, bintrayOrganization, bintrayRepository}
import com.mle.jenkinsctrl.http.JenkinsClient
import com.mle.jenkinsctrl.models.JobName
import com.mle.jenkinsctrl.{CredentialsReader, JenkinsCredentials}
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._

/**
  * A scala build file template.
  */
object SbtJenkinsBuild extends Build {

  lazy val sbtJenkins = Project("sbt-jenkins-control", file(".")).settings(projectSettings: _*)
  val mleGroup = "com.github.malliina"

  val logger = taskKey[Logger]("Logger helper")
  val jenkinsDefault = taskKey[Unit]("Builds the default Jenkins job")
  val jenkinsBuild = inputKey[Unit]("Builds a Jenkins job")
  val jenkinsCreds = settingKey[JenkinsCredentials]("Jenkins credentials")
  val jenkinsJob = inputKey[Unit]("A demo input task")

  lazy val jenkinsSettings = Seq(
    logger := streams.value.log,
    jenkinsCreds := new CredentialsReader().load,
    jenkinsDefault := jenkinsBuild.toTask(" musicmeta-compile").value,
    jenkinsBuild := {
      org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        .asInstanceOf[ch.qos.logback.classic.Logger]
        .setLevel(ch.qos.logback.classic.Level.WARN)
      val log = logger.value
      val names = spaceDelimited("<arg>").parsed
      names.headOption map { name =>
        using(new JenkinsClient(jenkinsCreds.value)) { client =>
          val build = client.buildWithProgressTask(JobName(name))
          build.consoleUpdates.subscribe(
            n => log.info(n.response),
            err => log.error(s"Build error: $err"),
            () => log.info("Build complete"))
          build.consoleUpdates.toBlocking.lastOption
        }
      } getOrElse {
        sys.error("Must specify a job name as parameter")
      }
    },
    jenkinsJob := {
      val log = logger.value
      val names = spaceDelimited("<arg>").parsed
      val stringified = names.mkString(", ")
      log.info(s"Parsed $stringified")
    }
  )

  lazy val projectSettings = jenkinsSettings ++ Seq(
    version := "0.0.1",
    scalaVersion := "2.10.6",
    organization := mleGroup,
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      mleGroup %% "util" % "2.0.0",
      mleGroup %% "jenkins-control" % "0.0.5"
    ),
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      Resolver.bintrayRepo("malliina", "maven")
    )
    //    bintrayOrganization in bintrayConf := None,
    //    bintrayRepository in bintrayConf := "sbt-plugins",
    //    publishMavenStyle := false
  )

  def using[T <: AutoCloseable, U](resource: T)(f: T => U) =
    try f(resource) finally resource.close()
}
