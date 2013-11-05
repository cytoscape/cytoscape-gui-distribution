#!/bin/bash
#
# Run cytoscape from a jar file
# This script is a UNIX-only (i.e. Linux, Mac OS, etc.) version
#-------------------------------------------------------------------------------

# First, see if help (-h, --help) or version (-v, --version) command line arguments
# are specified. If so, display help or the current version and exit.
# Note: Current version to be implemented after 3.1
if [[ $# > 0 ]]; then
	if [ $1 == "-h" -o $1 == "--help" ]; then
		cat <<-EOF
		
	Cytoscape Command-line Arguments
	================================
	usage: cytoscape.{sh|bat} [OPTIONS]
	 -h,--help             Print this message.
	 -v,--version          Print the version number.
	 -s,--session <file>   Load a cytoscape session (.cys) file.
	 -N,--network <file>   Load a network file (any format).
	 -T,--table <file>     Load a data table file (any table format).
	 -P,--props <file>     Load cytoscape properties file (Java properties
	                       format) or individual property: -P name=value.
	 -V,--vizmap <file>    Load vizmap properties file (Cytoscape VizMap
	                       format).
	 -S,--command <file>   Execute commands from script file
	 -R,--rest <port>      Start a rest service
	 
	EOF
		exit 0
	fi
fi

DEBUG_PORT=12345

script_path="$(dirname -- $0)"
if [ -h $0 ]; then
	link="$(readlink $0)"
	script_path="$(dirname -- $link)"
fi

export JAVA_DEBUG_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${DEBUG_PORT}"
if [ `uname` = "Darwin" ]; then
	CYTOSCAPE_MAC_OPTS="-Xdock:icon=cytoscape_logo_512.png -Xdock:name=Cytoscape"
fi

#vm_options_path=$HOME/.cytoscape
vm_options_path=$script_path

# Attempt to generate Cytoscape.vmoptions if it doesn't exist!
if [ ! -e "$vm_options_path/Cytoscape.vmoptions"  -a  -x "$script_path/gen_vmoptions.sh" ]; then
    "$script_path/gen_vmoptions.sh"
fi

export JAVA_OPTS=-Xmx1550M
if [ -r $vm_options_path/Cytoscape.vmoptions ]; then
		JAVA_OPTS=`cat $vm_options_path/Cytoscape.vmoptions`
else # Just use sensible defaults.
    echo '*** Missing Cytoscape.vmoptions, falling back to using defaults!'
		# Initialize MAX_MEM to something reasonable
		JAVA_OPTS=-Xmx1550M
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
export KARAF_OPTS=-Xms128M\ -Duser.dir="$PWD"\ -Dcytoscape.home="$CYTOSCAPE_HOME_ABS"\ -splash:CytoscapeSplashScreen.png\ "$CYTOSCAPE_MAC_OPTS"

export KARAF_DATA="${HOME}/CytoscapeConfiguration/3/karaf_data"
mkdir -p "${KARAF_DATA}/tmp"

$script_path/framework/bin/karaf "$@"
