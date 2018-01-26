package zzzbot

object Main {
  def main(args: Array[String]): Unit =
    Util.str("bot.platform", "") match {
      case "irc" =>
        IrcBot.run()
      case "slack" =>
        SlackBot.run()
      case s =>
        println(s"unknown -Dbot.platform parameter: '$s'")
    }
}
