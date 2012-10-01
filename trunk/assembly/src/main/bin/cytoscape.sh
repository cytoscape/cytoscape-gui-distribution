#!/bin/bash
#
# Run cytoscape from a jar file
# This script is a UNIX-only (i.e. Linux, Mac OS, etc.) version
#-------------------------------------------------------------------------------

DEBUG_PORT=12345

script_path="$(dirname -- $0)"
if [ -h $0 ]; then
	link="$(readlink $0)"
	script_path="$(dirname -- $link)"
fi

export JAVA_DEBUG_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${DEBUG_PORT}"
if [ `uname` = "Darwin" ]; then
	CYTOSCAPE_MAC_OPTS="-Xdock:icon=cytoscape_logo_512.png"
fi

#vm_options_path=$HOME/.cytoscape
vm_options_path=$script_path

# Attempt to generate Cytoscape.vmoptions if it doesn't exist!
if [ ! -e "$vm_options_path/Cytoscape.vmoptions"  -a  -x "$script_path/gen_vmoptions.sh" ]; then
    "$script_path/gen_vmoptions.sh"
fi

if [ -r $vm_options_path/Cytoscape.vmoptions ]; then
		export JAVA_MAX_MEM=`cat $vm_options_path/Cytoscape.vmoptions`
else # Just use sensible defaults.
    echo '*** Missing Cytoscape.vmoptions, falling back to using defaults!'
		# Initialize MAX_MEM to something reasonable
		export JAVA_MAX_MEM=1550M
fi

# The Cytoscape home directory contains the "framework" directory
# and this script.
CYTOSCAPE_HOME_REL=$script_path
CYTOSCAPE_HOME_ABS=`cd "$CYTOSCAPE_HOME_REL"; pwd`

PWD=$(pwd) 
# The user working directory needs to be explecitly set in -Duser.dir to current
# working directory since KARAF changes it to the framework directory. There
# might unforeseeable problems with this since the reason for KARAF setting the 
# working directory to framework is not known.
export KARAF_OPTS=-Xss10M\ -Duser.dir="$PWD"\ -Dcytoscape.home="$CYTOSCAPE_HOME_ABS"\ -splash:CytoscapeSplashScreen.png\ "$CYTOSCAPE_MAC_OPTS"

export KARAF_DATA="${HOME}/CytoscapeConfiguration/3/karaf_data"
mkdir -p "${KARAF_DATA}/tmp"

$script_path/framework/bin/karaf "$@"
