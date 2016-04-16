package com.malliina.sbt.jenkinsctrl

import com.malliina.jenkinsctrl.http.{BuildTask, JenkinsClient}
import com.malliina.jenkinsctrl.models.{BuildOrder, ConsoleProgress, JobName}
import com.malliina.jenkinsctrl.{JenkinsCredentials, JenkinsCredentialsReader}
import com.malliina.sbt.jenkinsctrl.JenkinsKeys._
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Properties, Try}

object JenkinsPlugin extends JenkinsPlugin

trait JenkinsPlugin {
  val settings = Seq(
    logger := streams.value.log,
    jenkinsCreds := Try(new JenkinsCredentialsReader().load).toOption,
    jenkinsReadCreds := {
      def fail = sys.error(s"No Jenkins credentials specified. See SBT setting `jenkinsCreds`.")
      jenkinsCreds.value.getOrElse(fail)
    },
    jenkinsOverview := {
      val future = withClient(jenkinsReadCreds.value)(_.overview())
      Await.result(future, 10.seconds)
    },
    jenkinsOverviewPrint := {
      val overview = jenkinsOverview.value
      val log = logger.value
      val line = s"Mode: ${overview.mode}, executors: ${overview.numExecutors}, quieting down: ${overview.quietingDown}"
      val jobs = overview.jobs.map(job => s"${job.name} at ${job.url}")
      val lineSep = Properties.lineSeparator
      val jobLines = jobs mkString lineSep
      log.info(s"$line$lineSep$jobLines")
    },
    jenkinsDefaultBuild := None,
    jenkinsBuildDefault := {
      val log = logger.value
      val buildSpecs = jenkinsDefaultBuild.value
      buildSpecs
        .map(specs => runLogged(specs, jenkinsReadCreds.value, log))
        .getOrElse(sys.error(s"No default job specified. Please initialize setting `jenkinsDefaultBuild`."))
    },
    jenkinsBuild := {
      val log = logger.value
      val order = jenkinsReadBuild.evaluated
      runLogged(order, jenkinsReadCreds.value, log)
    },
    jenkinsTask := {
      runJenkins(jenkinsReadBuild.evaluated, jenkinsReadCreds.value, logger.value)
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
    followBuild(runJenkins(order, creds, log), log)
  }

  def followBuild(build: BuildTask, log: Logger): Option[ConsoleProgress] = {
    val job = build.job
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

  /** Runs `order` using the Jenkins `creds`.
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

  def withClient[T](creds: JenkinsCredentials)(code: JenkinsClient => Future[T]): Future[T] = {
    val client = new JenkinsClient(creds)
    val future = code(client)
    future.onComplete(_ => client.close())
    future
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
