package com.mle.sbt.jenkinsctrl

import com.mle.jenkinsctrl.http.{BuildTask, JenkinsClient}
import com.mle.jenkinsctrl.models.{BuildOrder, ConsoleProgress, JobName}
import com.mle.jenkinsctrl.{CredentialsReader, JenkinsCredentials}
import com.mle.sbt.jenkinsctrl.JenkinsKeys._
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._

/**
  * @author mle
  */
object JenkinsPlugin extends JenkinsPlugin

trait JenkinsPlugin {
  val settings = Seq(
    logger := streams.value.log,
    jenkinsCreds := new CredentialsReader().load,
    jenkinsDefaultBuild := None,
    jenkinsBuildDefault := {
      val log = logger.value
      val order = jenkinsDefaultBuild.value
      order
        .map(o => runLogged(o, jenkinsCreds.value, log))
        .getOrElse(log.error(s"No default job specified. Please initialize setting `jenkinsDefaultBuild`."))
    },
    jenkinsBuild := {
      val log = logger.value
      val order = jenkinsReadBuild.evaluated
      runLogged(order, jenkinsCreds.value, log)
    },
    jenkinsTask := {
      runJenkins(jenkinsReadBuild.evaluated, jenkinsCreds.value, logger.value)
    },
    jenkinsReadBuild := {
      val params = spaceDelimited("<arg>").parsed.toList
      params match {
        case jobName :: parameters =>
          val kvs = parameters.flatMap(param => param.split("=") match {
            case Array(key, value) => Option((key, value))
            case _ => None
          }).toMap
          BuildOrder(JobName(jobName), kvs)
        case Nil =>
          sys.error("Must specify a job name as parameter")
      }
    }
  )

  def runLogged(order: BuildOrder, creds: JenkinsCredentials, log: Logger): Option[ConsoleProgress] = {
    followBuild(order.job, runJenkins(order, creds, log), log)
  }

  def followBuild(job: JobName, build: BuildTask, log: Logger): Option[ConsoleProgress] = {
    build.queueUpdates.subscribe(
      n => log.info(s"Job $job is in the build queue..."),
      err => log.error(s"Queueing failed: $err"),
      () => log.info(s"Job $job exited the build queue"))
    build.consoleUpdates.subscribe(
      n => log.info(n.response),
      err => log.error(s"Build error: $err"),
      () => log.info(s"Build of job $job complete"))
    build.consoleUpdates.toBlocking.lastOption
  }

  /**
    * Runs `order` using the Jenkins `creds`.
    *
    * @param order
    * @param creds
    * @param log
    * @return
    */
  def runJenkins(order: BuildOrder, creds: JenkinsCredentials, log: Logger): BuildTask = {
    val client = new JenkinsClient(creds)
    val parameters = order.parameters
    val jobName = order.job
    val paramsString = separateWith("=", parameters) mkString " "
    val suffix = if (parameters.isEmpty) "" else s" with parameters $paramsString"
    log.info(s"Building job $jobName$suffix")
    //    turnOffLogback()
    val build = client.buildWithProgressTask(order)
    // cleans up when the build completes
    build.consoleUpdates.subscribe(_ => (), err => client.close(), () => client.close())
    build
  }

  def separateWith[K, V](separator: String, map: Map[K, V]) =
    map.map {
      case (key, value) => s"$key$separator$value"
    }.toList

  def turnOffLogback() = {
    org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[ch.qos.logback.classic.Logger]
      .setLevel(ch.qos.logback.classic.Level.WARN)
  }
}
