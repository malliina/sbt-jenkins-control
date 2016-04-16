package com.malliina.sbt.jenkinsctrl

import com.malliina.jenkinsctrl.JenkinsCredentials
import com.malliina.jenkinsctrl.http.BuildTask
import com.malliina.jenkinsctrl.models.{BuildOrder, Overview}
import sbt._

object JenkinsKeys extends JenkinsKeys

trait JenkinsKeys {
  val logger = taskKey[Logger]("Logger helper")

  val jenkinsOverview = taskKey[Overview]("Gets an overview of Jenkins")
  val jenkinsOverviewPrint = taskKey[Unit]("Prints an overview of Jenkins")
  val jenkinsDefaultBuild = settingKey[Option[BuildOrder]]("The default Jenkins job to build")
  val jenkinsBuild = inputKey[Unit]("Builds the Jenkins job supplied as input")
  val jenkinsBuildDefault = taskKey[Unit]("Runs the build defined in `jenkinsDefaultBuild`")
  val jenkinsTask = inputKey[BuildTask]("Builds a Jenkins job, returning a task")
  val jenkinsReadBuild = inputKey[BuildOrder]("Reads a build order from the command line")
  val jenkinsCreds = settingKey[Option[JenkinsCredentials]]("Jenkins credentials")
  val jenkinsReadCreds = taskKey[JenkinsCredentials]("Reads credentials, failing if they cannot be read")
}
