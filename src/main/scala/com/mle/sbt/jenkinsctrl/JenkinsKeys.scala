package com.mle.sbt.jenkinsctrl

import com.mle.jenkinsctrl.JenkinsCredentials
import com.mle.jenkinsctrl.http.BuildTask
import com.mle.jenkinsctrl.models.BuildOrder
import sbt._

object JenkinsKeys extends JenkinsKeys

trait JenkinsKeys {
  val logger = taskKey[Logger]("Logger helper")

  val jenkinsDefaultBuild = settingKey[Option[BuildOrder]]("The default Jenkins job to build")
  val jenkinsBuild = inputKey[Unit]("Builds the Jenkins job supplied as input")
  val jenkinsBuildDefault = taskKey[Unit]("Runs the build defined in `jenkinsDefaultBuild`")
  val jenkinsTask = inputKey[BuildTask]("Builds a Jenkins job, returning a task")
  val jenkinsReadBuild = inputKey[BuildOrder]("Reads a build order from the command line")
  val jenkinsCreds = settingKey[JenkinsCredentials]("Jenkins credentials")
}
