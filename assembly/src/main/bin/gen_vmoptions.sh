#!/bin/sh
# Generates the Cytoscape.vmoptions file

script_path="$(dirname -- $0)"

#vm_options_path="$HOME/.cytoscape"
vm_options_path=$script_path

if [ ! -e $vm_options_path ]; then
    /bin/mkdir $vm_options_path
fi

# Determine amount of physical memory present:
if [ `uname` = "Darwin" ]; then
    phys_mem=`sysctl -a | grep 'hw.memsize:' | sed 's/[ ][ ][ ]*/ /g' | cut -f 2 -d ' '`
    if [ -z $phys_mem ]; then
         phys_mem=`sysctl -a | grep 'hw.memsize =' | sed 's/[ ][ ][ ]*/ /g' | cut -f 3 -d ' '`
    fi
    phys_mem=$((phys_mem / 1024 / 1024)) # Convert from B to MiB
else # We assume Linux
    phys_mem=`cat /proc/meminfo | grep 'MemTotal:' | sed 's/[ ][ ][ ]*/ /g' | cut -f 2 -d ' '`
    phys_mem=$((phys_mem / 1024)) # Convert from KiB to MiB
fi

# Now we know the amount of physical memory, but we don't want to try to use it all
minmem=768
maxmem=768

if `java -version 2>&1 | grep -- 64-Bit > /dev/null`; then # We have a 64 bit JVM.
	if [ $phys_mem -gt 3071 ]; then
		minmem=2048 
	    maxmem=$((phys_mem-1024))
	else
		if [ $phys_mem -gt 1535 ]; then 
	    	maxmem=1024
		elif [ $phys_mem -gt 2047 ]; then 
	    	maxmem=1536
	    fi
	    minmem=${maxmem}
	fi
else # Assume a 32 bit JVM.
	if [ $phys_mem -gt 2047 ]; then
	    maxmem=1550
	elif [ $phys_mem -gt 1535 ]; then
	    maxmem=1024
	fi
	minmem=${maxmem}
fi

echo "-Xms"${minmem}M> "$vm_options_path/Cytoscape.vmoptions"
echo "-Xmx"${maxmem}M>> "$vm_options_path/Cytoscape.vmoptions"

exit 0
