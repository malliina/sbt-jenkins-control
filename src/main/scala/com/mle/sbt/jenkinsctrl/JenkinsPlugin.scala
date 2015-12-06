package com.mle.sbt.jenkinsctrl

import com.mle.sbt.jenkinsctrl.JenkinsKeys._
import sbt.Keys.streams

/**
  * @author mle
  */
object JenkinsPlugin extends JenkinsPlugin

trait JenkinsPlugin {
  val settings = Seq(
    jenkinsBuild := {
      logger.value.info("Building")
      ???
    },
    logger := streams.value.log
  )
}
