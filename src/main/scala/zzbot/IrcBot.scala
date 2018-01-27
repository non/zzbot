package zzbot

import org.jibble.pircbot.PircBot

object IrcBot extends PircBot with AbstractBot {

  // concrete members

  // note: channels always start with #, users never do.
  type Id = String
  type Name = String
  type Channel = String

  def parseChannel(s: String): Option[Channel] =
    Util.parseHashChannel(s)

  def run(): Unit = {
    setName(botName)
    setVerbose(true)
    setEncoding("UTF-8")
    connect()
  }

  def quit(): Nothing = {
    disconnect()
    System.exit(1)
    sys.error("unreachable")
  }

  def join(ch: Channel): Unit =
    joinChannel(ch)

  def leave(ch: Channel): Unit =
    partChannel(ch)

  def send(ch: Channel, msg: String): Unit =
    msg.split("\n")
      .iterator
      .filter(! _.isEmpty)
      .take(5)
      .foreach(m => sendLine(ch, " " + munge(m)))

  def munge(s: String): String =
    if (!s.isEmpty && s.charAt(0) == '\r') s.substring(1) else s

  def sendLine(ch: Channel, line: String): Unit =
    sendMessage(ch, line)

  def preprocess(code: String): String =
    code

  def postprocess(code: String): String =
    code

  def name(s: String): Name = s
  def id(s: String): Id = s
  def nameToId(name: String): String = name
  def idToName(id: String): String = id

  def mention(name: String): String =
    name + ":"

  // platform-dependent implementation

  lazy val server: String =
    Util.str("bot.server", "irc.freenode.net")

  lazy val channels: List[Channel] =
    Util.strs("bot.channels", List("#d_m_private"))

  def connect(): Unit =
    connect(server)

  override def onConnect(): Unit =
    channels.foreach(join)

  override def onDisconnect(): Unit =
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
