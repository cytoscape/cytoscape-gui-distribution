#!/bin/bash

DEBUG_PORT=12345

cd $(dirname $0)

export JAVA_MAX_MEM=1550M

export JAVA_DEBUG_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${DEBUG_PORT}"
export KARAF_OPTS="-Xss10M -splash:CytoscapeSplashScreen.png -Xdock:icon=cytoscape_logo_512.png"

framework/bin/karaf "$@"
