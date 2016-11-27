package com.example.controller

import akka.actor.ActorSystem
import com.example.Settings
import slack.rtm.SlackRtmClient

object SlackController {
  implicit val system = ActorSystem("slack")
  implicit val ec = system.dispatcher

  val client = SlackRtmClient(Settings.slack.token)

  // これがないと、初回のsendMessageが失敗する？
  Thread.sleep(5000L)

  private val channelId = client.state.getChannelIdForName(Settings.slack.postChName).get

  def sendMessage(text: String) = {
    client.sendMessage(channelId, text)
  }
}
