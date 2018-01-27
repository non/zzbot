package zzbot

import java.io.{PrintStream, ByteArrayOutputStream}
import scala.collection.mutable
import scala.tools.nsc
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results

trait AbstractBot {

  // abstract members

  type Id
  type Name
  type Channel

  def parseChannel(s: String): Option[Channel]

  def run(): Unit
  def quit(): Nothing
  def join(ch: Channel): Unit
  def leave(ch: Channel): Unit
  def send(ch: Channel, msg: String): Unit

  def preprocess(code: String): String

  def postprocess(code: String): String

  def name(s: String): Name
  def id(s: String): Id
  def nameToId(name: Name): Id
  def idToName(id: Id): Name

  def mention(name: Name): String

  // platform-independent implementation

  lazy val botName: Name =
    name(Util.str("bot.name", "zzbot"))

  lazy val admins: Set[Name] =
    Util.prop("bot.admins", Set("d_m"))(_.split(",").toSet).map(name)

  lazy val imports: List[String] =
    Util.strs("bot.imports", Nil)

  // paths to compiler plugins
  lazy val plugins: List[String] =
    Util.strs("bot.plugins", Nil)

  final val savedOut = System.out
  final val savedErr = System.err
  final val baos = new ByteArrayOutputStream
  final val printStream = new PrintStream(baos)

  case class Msg(channel: Channel, sender: Id, message: String)

  def captureOutput(block: => Unit): Unit =
    try {
      System.setOut(printStream)
      System.setErr(printStream)
      Console.withOut(printStream)(Console.withErr(printStream)(block))
    } finally {
      System.setOut(savedOut)
      System.setErr(savedErr)
      baos.flush()
      baos.reset()
    }

  def newInterpreter(): IMain = {
    val settings = new nsc.Settings(null)
    settings.plugin.value = plugins
    settings.YpartialUnification.value = true
    settings.usejavacp.value = true
    settings.deprecation.value = true
    settings.feature.value = false
    val si = new IMain(settings)
    imports.foreach(pkg => si.quietRun(s"import $pkg"))
    si
  }

  val interpreters: mutable.Map[Channel, IMain] =
    mutable.Map.empty[Channel, IMain]

  def interpreter(channel: Channel)(f: (IMain, ByteArrayOutputStream) => Unit): Unit =
    this.synchronized {
      val si = interpreters.getOrElseUpdate(channel, newInterpreter())
      captureOutput(f(si, baos))
    }

  // FIXME: -Dscala.color breaks these regexes, but i'm not sure if
  // they are actually needed or not. it seems nice to know how to
  // refer to output (e.g. res33) although possibly it's a bit junky.
  def interpret(si: IMain, prog: String, cout: ByteArrayOutputStream): String =
    si.interpret(preprocess(prog)) match {
      case Results.Success =>
        postprocess(cout.toString)
      case Results.Error =>
        savedOut.println(s"failed: $prog")
        postprocess(cout.toString)
      case Results.Incomplete =>
        "error: incomplete expression"
    }

  def authenticate(senderId: Id, cmd: String)(body: => Unit): Unit = {
    val sender = idToName(senderId)
    if (admins(sender)) {
      savedOut.println(s"$sender asked us to $cmd")
      body
    } else {
      savedOut.println(s"$sender does not have permission to $cmd")
    }
  }

  val Cmd = """^([^ ]+) ((?:.|\n)*)$""".r

  def receive(msg: Msg): Unit =
    msg.message match {
      case Cmd("!", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, interpret(si, m, cout))
        }
      case Cmd(s, m) if s == mention(botName) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, interpret(si, m, cout))
        }

      case Cmd(":type", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, postprocess(si.typeOfExpression(m).directObjectString))
        }
      case Cmd(":reify", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, interpret(si, s"_root_.scala.reflect.runtime.universe.reify { $m }", cout))
        }
      case Cmd(":time", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, interpret(si, s"_root_.zzbot.Util.timer { $m }", cout))
        }
      case Cmd(":sizeof", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, interpret(si, s"_root_.zzbot.Util.sizeOf($m)", cout))
        }
      case Cmd(":fullsizeof", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, interpret(si, s"_root_.zzbot.Util.fullSizeOf($m)", cout))
        }
      case Cmd(":staticsizeof", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, interpret(si, s"_root_.zzbot.Util.staticSizeOf($m)", cout))
        }

      case ":reset" =>
        interpreters -= msg.channel
      case ":reset-all" =>
        interpreters.clear()
      case ":quit" =>
        authenticate(msg.sender, ":quit") { quit() }
      case Cmd(":join", m) =>
        authenticate(msg.sender, ":join") { parseChannel(m).foreach(join(_)) }
      case Cmd(":leave", m) =>
        authenticate(msg.sender, ":leave") { parseChannel(m).foreach(leave(_)) }

      case s => ()
    }
}
