package zzbot

import java.io.{PrintStream, ByteArrayOutputStream}
import scala.collection.mutable
import scala.tools.nsc
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results

trait AbstractBot {

  // abstract members

  type Channel

  def parseChannel(s: String): Option[Channel]

  def run(): Unit
  def quit(): Nothing
  def join(ch: Channel): Unit
  def leave(ch: Channel): Unit
  def send(ch: Channel, msg: String): Unit

  // platform-independent implementation

  lazy val admins: Set[String] =
    Util.prop("bot.admins", Set("d_m"))(_.split(",").toSet)

  lazy val imports: List[String] =
    Util.strs("bot.imports", Nil)

  // paths to compiler plugins
  lazy val plugins: List[String] =
    Util.strs("bot.plugins", Nil)

  final val savedOut = System.out
  final val savedErr = System.err
  final val baos = new ByteArrayOutputStream
  final val printStream = new PrintStream(baos)

  case class Msg(channel: Channel, sender: String, message: String)
  
  def munge(s: String): String =
    if (!s.isEmpty && s.charAt(0) == '\r') s.substring(1) else s

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

  def sendLines(channel: Channel, message: String): Unit =
    message.split("\n")
      .iterator
      .filter(! _.isEmpty)
      .take(5)
      .foreach(m => send(channel, " " + munge(m)))

  // FIXME: -Dscala.color breaks these regexes, but i'm not sure if
  // they are actually needed or not. it seems nice to know how to
  // refer to output (e.g. res33) although possibly it's a bit junky.
  def interpret(si: IMain, prog: String, cout: ByteArrayOutputStream): String =
    si.interpret(prog) match {
      case Results.Success =>
        cout.toString.replaceAll("(?m:^res[0-9]+: +)", "")
      case Results.Error =>
        cout.toString.replaceAll("^<console>:[0-9]+: +", "")
      case Results.Incomplete =>
        "error: incomplete expression"
    }

  val Cmd = """^([^ ]+) (.*)$""".r

  def receive(msg: Msg): Unit =
    msg.message match {
      case Cmd("!", m) =>
        interpreter(msg.channel) { (si, cout) =>
          sendLines(msg.channel, interpret(si, m, cout))
        }

      case Cmd(":type", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, si.typeOfExpression(m).directObjectString)
        }
      case ":reset" =>
        interpreters -= msg.channel
      case ":reset-all" =>
        interpreters.clear()
      case Cmd(":sizeof", m) =>
        interpreter(msg.channel) { (si, cout) =>
          sendLines(msg.channel, interpret(si, s"_root_.zzbot.Util.sizeOf($m)", cout))
        }
      case Cmd(":fullsizeof", m) =>
        interpreter(msg.channel) { (si, cout) =>
          sendLines(msg.channel, interpret(si, s"_root_.zzbot.Util.fullSizeOf($m)", cout))
        }
      case Cmd(":staticsizeof", m) =>
        interpreter(msg.channel) { (si, cout) =>
          sendLines(msg.channel, interpret(si, s"_root_.zzbot.Util.staticSizeOf($m)", cout))
        }
      case Cmd(":reify", m) =>
        interpreter(msg.channel) { (si, cout) =>
          sendLines(msg.channel, interpret(si, s"_root_.scala.reflect.runtime.universe.reify { $m }", cout))
        }
      case Cmd(":time", m) =>
        interpreter(msg.channel) { (si, cout) =>
          sendLines(msg.channel, interpret(si, s"_root_.zzbot.Util.timer { $m }", cout))
        }
      case ":quit" =>
        if (admins(msg.sender)) quit()
      case Cmd(":join", m) =>
        if (admins(msg.sender)) parseChannel(m).foreach(join(_))
      case Cmd(":leave", m) =>
        if (admins(msg.sender)) parseChannel(m).foreach(leave(_))

      case _ =>
        ()
    }
}
