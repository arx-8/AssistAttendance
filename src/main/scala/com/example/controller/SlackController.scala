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

  private val channelId = getChannelIdBy(client, Settings.slack.postChName)

  def sendMessage(text: String) = {
    client.sendMessage(channelId, text)
  }

  private def suppressLogging() = {
    val logger = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    logger.stop()
  }

  /**
    * チャンネルIDを返す。
    * 公開・非公開チャンネル、両対応。
    *
    * @param channelName
    * @return
    */
  private def getChannelIdBy(client: SlackRtmClient, channelName: String): String = {
    // public ch
    client.state.getChannelIdForName(channelName).getOrElse {
      // private ch
      client.state.groups.find(_.name == channelName).map(_.id) match {
        case Some(chId) => chId
        case None => throw new NoSuchElementException(s"Slackのチャンネル名「$channelName」が見つかりませんでした。")
      }
    }
  }
}
