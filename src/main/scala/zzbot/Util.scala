package zzbot

import clouseau.{Calculate, Inst, Units}

object Util {

  def prop[A](name: String, empty: => A)(f: String => A): A =
    Option(System.getProperty(name)).map(f).getOrElse(empty)

  def str(name: String, empty: => String): String =
    prop(name, empty)(identity)

  def strs(name: String, empty: => List[String]): List[String] =
    prop(name, empty)(_.split(",").toList)

  val HashPattern = """^(#[_a-zA-Z][-_a-zA-Z0-9]*)$""".r

  def parseHashChannel(s: String): Option[String] =
    s match {
      case HashPattern(ch) => Some(ch)
      case _ => None
    }

  def sizeOf(a: AnyRef): String =
    if (Inst.initialized) Units.approx(Calculate.sizeOf(a))
    else "instrumentation is not installed"

  def staticSizeOf(a: AnyRef): String =
    if (Inst.initialized) Units.approx(Calculate.staticSizeOf(a))
    else "instrumentation is not installed"

  def fullSizeOf(a: AnyRef): String =
    if (Inst.initialized) Units.approx(Calculate.fullSizeOf(a))
    else "instrumentation is not installed"

  import ichi.bench.Thyme

  val th = new Thyme(watchLoads = false, watchGarbage = false, watchMemory = false)

  def disp(t: Long): String =
    if (t < 1000) "%dns" format t
    else if (t < 1000000) "%.1fµs" format (t / 1000.0)
    else if (t < 1000000000) "%.2fms" format (t / 1000000.0)
    else "%.3fs" format (t / 1000000000.0)

  def fmt(n: Double): String = disp((n * 1000000000).toLong)

  def timer[A](f: => A): A = {
    val br = Thyme.Benched.empty
    val a = th.bench(f)(br, effort = 1)
    val (m, r, e) = (fmt(br.runtime), br.runtimeIterations, fmt(br.runtimeError))
    System.out.println(s"averaged $m over $r runs (± $e)")
    a
  }
}
