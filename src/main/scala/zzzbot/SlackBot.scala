package org.multibot

import slack.rtm.SlackRtmClient
import akka.actor.ActorSystem

class SlackBot(apiToken: String) extends AbstractBot {

  var client: SlackRtmClient = null

  type Channel = String

  def run(): Unit = {
    client = SlackRtmClient(apiToken)(ActorSystem("slack"))
    client.onMessage(m => receive(Msg(m.channel, m.user, m.text)))
  }

  def send(ch: Channel, msg: String): Unit =
    client.sendMessage(ch, msg)
}
