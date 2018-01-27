## zzbot

### dedication

```
Say, black sheep, black, do you got some wool?
Yes, I do, man, my bag is full.

It's the root of evil and you know the rest.
But it's way ahead of what's second best!

-- ZZ Top, "Just Got Paid"
```

### overview

ZZBot is intended to be a multi-platform bot for evaluating Scala
code. Currently it supports IRC and Slack.

### demo

![Screenshot of the bot in action](https://github.com/non/zzbot/raw/master/demo.png)

### interacting with the bot

The primary point of the bot is to evaluate code that you send to it.
There are two basic ways to signal that you're sending code to be
evaluated:

 * Prefix with an exclamation point, `! <EXPR>`
  * For example, `! val x = 9`.
  + The space is mandatory, i.e. `!val x= 9` won't work.
  + To evaluate `!x` you'd need to say `! !x`
 * Prefix with a mention of the bot, `<MENTION> <EXPR>`
  + On IRC, this would be `zzbot: val x = 9`
  + On Slack, this would be `@zzbot val x = 9`

In addition, the bot supports a number of auxilliary commands:

 * `:type <EXPR>` - display resulting type of the expression
 * `:reify <EXPR>` - show the correspoding tree for the expression
 * `:time <EXPR>` - run the expression through a (rough) benchmark
 * `:sizeof <EXPR>` - compute *size* of the given value
 * `:fullsizeof <EXPR>` - compute *full size* of the given value
 * `:staticsizeof <EXPR>` - compute *static size* of the given value
 * `:reset` - reset the REPL for this channel
 * `:reset-all` - reset the REPLs for all channels
 * `:quit` - tell the bot to quit (**admin-only**)
 * `:join <CHANNEL>` - tell the bot to join a channel (**admin-only, irc-only**)
 * `:leave <CHANNEL>` - tell the bot to leave a channel (**admin-only, irc-only**)

The benchmarking uses [thyme](https://github.com/Ichoran/thyme). For
serious benchmarking, please consider using [JMH](http://openjdk.java.net/projects/code-tools/jmh/)
(e.g. via the [sbt-jmh](https://github.com/ktoso/sbt-jmh) plugin).

The sizing info is provided by
[clouseau](https://github.com/non/clouseau) and should be interpreted
using [its documentation](https://github.com/non/clouseau#details).

### details

To use the bot, you'll want to:

 1. clone this repo (e.g. `git clone https://github.com/non/zzbot.git`)
 2. assemble the jar file (e.g. `sbt assembly`)
 3. customize the launcher (e.g. `emacs run.sh`)
 4. run the bot! (e.g. `./run.sh`)

### platforms

You can select the desired platform via `-Dbot.platform`, for example:

 * `-Dbot.platform=irc`: run an IRC bot
 * `-Dbot.platform=slack`: run a Slack bot

The shared options are:

 * `-Dbot.admins`: the IRC users who can administrate the bot (e.g. `billy,frank,dusty`)
 * `-Dbot.imports`: any imports to run when the REPL starts (e.g. `cats._`)
 * `-Dbot.plugins`: any compiler plugin jars to include (e.g. `path/to/plugin.jar`)

The IRC options are:

 * `-Dbot.name`: the nickname for the bot to use (e.g. `mybot`)
 * `-Dbot.server`: the IRC server to connect to (default: `irc.freenode.net`)
 * `-Dbot.channels`: the IRC channels to connect to (e.g. `#scala`)

The slack option is:

 * `-Dbot.token`: the Slack API token to use (keep this secret!)

### future work

There are a number of avenues for future work:

 1. Distribute an executable jar
 2. Support more platforms (e.g. Twitter, Gitter, SMS, etc.)
 3. Consider using Coursier to dynamically load libraries
 4. Make compiler plugin support less hacky
 5. Consider adding more user commands
 6. Consider adding more admin commands
 7. Improve the code
 8. Figure out how to write useful tests

### copyright and license

All code is available to you under the Apache 2 license, available at
https://www.apache.org/licenses/LICENSE-2.0.

ZZBot is based on code from [Multibot](https://github.com/lopex/multibot) and [Spirebot](https://github.com/non/spirebot).

Multibot is copyright Marcin Mielżyński, 2012-2017.

Spirebot is copyright Erik Osheim, 2013-2014.

ZZBot is copyright Erik Osheim, 2018.
