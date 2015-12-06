package com.mle.sbt.jenkinsctrl

import com.mle.jenkinsctrl.http.BuildTask
import sbt._

object JenkinsKeys extends JenkinsKeys

trait JenkinsKeys {
  val jenkinsBuild = taskKey[BuildTask]("Builds a Jenkins job")
  val logger = taskKey[Logger]("Logger helper")
}
