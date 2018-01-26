package org.multibot

import org.jibble.pircbot.PircBot

class IrcBot(server: String = "irc.freenode.net") extends PircBot with AbstractBot {

  // concrete members

  // note: channels always start with #, users never do.
  type Channel = String

  def run(): Unit = {
    setName(botName)
    setVerbose(true)
    setEncoding("UTF-8")
    connect()
  }

  def send(ch: Channel, msg: String): Unit =
    sendMessage(ch, msg)

  // platform-dependent implementation

  lazy val botName = str("stripebot.name", "stripebot")
  lazy val channels = strs("stripebot.channels", List("d_m_private"))

  def connect(): Unit = {
    connect(server)
    channels.foreach(joinChannel)
  }

  override def onDisconnect: Unit =
    while (true) {
      try {
        connect()
        return ()
      } catch { case e: Exception =>
        e.printStackTrace
        Thread.sleep(10000)
      }
    }

  override def onPrivateMessage(sender: String, login: String, hostname: String, message: String): Unit =
    receive(Msg(sender, sender, message))

  override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) =
    receive(Msg(channel, sender, message))
}
