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
