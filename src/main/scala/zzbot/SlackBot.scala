package zzbot

import slack.rtm.SlackRtmClient
import akka.actor.ActorSystem

object SlackBot extends AbstractBot {

  lazy val apiToken: String =
    Util.prop("bot.token", Option.empty[String])(Some(_)).getOrElse("missing -Dbot.token")

  var client: SlackRtmClient = null

  type Channel = String

  def parseChannel(s: String): Option[Channel] =
    Util.parseHashChannel(s)

  // FIXME: can bots do these in slack?
  def join(ch: Channel): Unit = ()
  def leave(ch: Channel): Unit = ()

  def run(): Unit = {
    client = SlackRtmClient(apiToken)(ActorSystem("slack"))
    client.onMessage(m => receive(Msg(m.channel, m.user, m.text)))
  }

  def quit(): Nothing = {
    client.close()
    System.exit(1)
    sys.error("unreachable")
  }

  def send(ch: Channel, msg: String): Unit =
    client.sendMessage(ch, msg)
}
