#!/bin/sh

export LCLANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

JAR=target/scala-2.12/zzbot-assembly-0.1.0.jar

# we're doing a weird trick here. our assembly jar is doing
# triple-duty as:
#
# 1. a scalac plugin (kind-projector)
# 2. a javaagent (clouseau)
# 3. the actual irc/slack bot
#
# this means we only have to distribute a single jar that we can run,
# which is a bit nicer than having multiple files lying around.

java \
    -javaagent:$JAR \
    -Dsun.jnu.encoding=UTF-8 \
    -Dfile.encoding=UTF-8 \
    -Dscala.color=true \
    -Dbot.platform=irc \
    -Dbot.name=zzbot \
    -Dbot.plugins=$JAR \
    -jar $JAR
