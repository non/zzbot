package zzbot

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
}
