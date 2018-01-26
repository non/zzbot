package org.multibot

import java.io.{PrintStream, ByteArrayOutputStream}
import scala.collection.mutable
import scala.tools.nsc
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results

trait AbstractBot {

  // abstract members

  type Channel

  def run(): Unit

  def send(ch: Channel, msg: String): Unit

  // platform-independent implementation

  def prop[A](name: String, empty: => A)(f: String => A): A =
    Option(System.getProperty(name)).map(f).getOrElse(empty)

  def str(name: String, empty: => String): String =
    prop(name, empty)(identity)

  def strs(name: String, empty: => List[String]): List[String] =
    prop(name, List.empty[String])(_.split(",").toList)

  lazy val imports = strs("stripebot.imports", Nil)

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
    settings.usejavacp.value = true
    settings.deprecation.value = true
    settings.feature.value = false
    val si = new IMain(settings)
    imports.foreach(pkg => si.quietRun(s"import $pkg"))
    si
  }

  val interpreters = mutable.Map.empty[Channel, IMain]

  def interpreter(channel: Channel)(f: (IMain, ByteArrayOutputStream) => Unit) =
    this.synchronized {
      val si = interpreters.getOrElseUpdate(channel, newInterpreter())
      captureOutput(f(si, baos))
    }

  def sendLines(channel: Channel, message: String) =
    message.split("\n")
      .iterator
      .filter(! _.isEmpty)
      .take(5)
      .foreach(m => send(channel, " " + munge(m)))

  val Cmd = """^([^ ]+) (.*)$""".r

  def receive(msg: Msg): Unit =
    msg.message match {
      case Cmd("!", m) =>
        interpreter(msg.channel) { (si, cout) =>
          sendLines(msg.channel, si.interpret(m) match {
            case Results.Success =>
              cout.toString.replaceAll("(?m:^res[0-9]+: )", "")
            case Results.Error =>
              cout.toString.replaceAll("^<console>:[0-9]+: ", "")
            case Results.Incomplete =>
              "error: unexpected EOF found, incomplete expression"
          })
        }
      case Cmd("!type", m) =>
        interpreter(msg.channel) { (si, cout) =>
          send(msg.channel, si.typeOfExpression(m).directObjectString)
        }
      case "!reset" =>
        interpreters -= msg.channel
      case "!reset-all" =>
        interpreters.clear()
      case _ =>
        ()
    }

  def main(): Unit =
    run()
}
