package zzbot

import akka.actor.ActorSystem
import java.util.concurrent.ConcurrentHashMap
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

object SlackBot extends AbstractBot {

  type Channel = String
  type Id = String
  type Name = String

  lazy val apiToken: String =
    Util.prop("bot.token", Option.empty[String])(Some(_)).getOrElse("missing -Dbot.token")

  var api: SlackApiClient = null
  var client: SlackRtmClient = null

  // currently we're only using all of this machinery to be able to
  // map the bot's ID to the bot's expected username, and the admin's
  // IDs to usernames. there's probably a better way to do this with
  // the slack API that i couldn't find.
  //
  // one thing we aren't doing is tracking name changes. in theory
  // onEvent(...) could be updating the maps following renames.
  //
  // we could also potentially do some fancier things in the future.
  @transient val idToNameMap = new ConcurrentHashMap[Id, Name]
  @transient val nameToIdMap = new ConcurrentHashMap[Name, Id]

  // NOTE: to support user renames we'll want methods to delete and/or
  // modify a user as well.
  def addUser(id: Id, name: Name): Unit = {
    idToNameMap.put(id, name)
    nameToIdMap.put(name, id)
  }

  def parseChannel(s: String): Option[Channel] =
    Util.parseHashChannel(s)

  // FIXME: can bots do these in slack?
  def join(ch: Channel): Unit = ()
  def leave(ch: Channel): Unit = ()

  def run(): Unit = {
    implicit val actorSystem = ActorSystem("slack")
    implicit val executionContext = actorSystem.dispatcher

    api = SlackApiClient(apiToken)
    client = SlackRtmClient(apiToken)
    client.onMessage(m => receive(Msg(m.channel, m.user, deescape(m.text))))
    client.onEvent(e => ()) // TODO
    api.listUsers.foreach(_.foreach(u => addUser(u.id, u.name)))
  }

  def quit(): Nothing = {
    client.close()
    System.exit(1)
    sys.error("unreachable")
  }

  def send(ch: Channel, msg: String): Unit =
    client.sendMessage(ch, escape(msg))

  val BlockRe = """^```\n((?:.|\n)+)\n```$""".r
  val CodeRe = """^ *`(.+)` *$""".r

  def escape(s: String): String =
    s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

  def deescape(s: String): String =
    s.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")

  def preprocess(code: String): String =
    code match {
      case BlockRe(prog) => prog
      case CodeRe(prog) => prog
      case s => s
    }

  def postprocess(code: String): String = {
    val s = code.trim
    if (s.contains('\n')) "```\n" + s + "\n```"
    else if (s.contains('`')) s"```$s```"
    else s"`$s`"
  }

  def name(s: String): Name = s
  def id(s: String): Id = s

  def nameToId(name: Name): Id =
    Option(nameToIdMap.get(name)) match {
      case Some(id) => id
      case None => "unknown"
    }

  def idToName(id: Id): Name =
    Option(idToNameMap.get(id)) match {
      case Some(name) => name
      case None => "???"
    }

  def mention(name: Name): String =
    "<@" + nameToId(name) + ">"
}
