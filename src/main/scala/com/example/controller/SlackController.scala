package com.example.controller

import akka.actor.ActorSystem
import ch.qos.logback.classic.LoggerContext
import com.example.Settings
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.slf4j.LoggerFactory
import slack.rtm.SlackRtmClient

import scala.language.postfixOps

object SlackController {
  suppressLogging()

  private val config = ConfigFactory.load()
    .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
    .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))
  implicit val system = ActorSystem("slack", config)
  implicit val ec = system.dispatcher

  val client = SlackRtmClient(Settings.slack.token)

  // これがないと、初回のsendMessageが失敗する？
  Thread.sleep(5000L)

  private val channelId = {
    val temp = client.state.getChannelIdForName(Settings.slack.postChName)
    temp match {
      case Some(chId) => chId
      case None => throw new NoSuchElementException("Slackのチャンネル名「" + Settings.slack.postChName + "」が見つかりませんでした。")
    }
  }

  def sendMessage(text: String) = {
    client.sendMessage(channelId, text)
  }

  private def suppressLogging() = {
    val logger = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    logger.stop()
  }
}
