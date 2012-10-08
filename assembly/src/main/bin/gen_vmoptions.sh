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
    phys_mem=`sysctl -a | grep 'hw.memsize =' | sed 's/[ ][ ][ ]*/ /g' | cut -f 3 -d ' '`
    phys_mem=$((phys_mem / 1024 / 1024)) # Convert from B to MiB
else # We assume Linux
    phys_mem=`cat /proc/meminfo | grep 'MemTotal:' | sed 's/[ ][ ][ ]*/ /g' | cut -f 2 -d ' '`
    phys_mem=$((phys_mem / 1024)) # Convert from KiB to MiB
fi

# Now we know the amount of physical memory, but we don't want to try to use it all
mem=768
if [ $phys_mem -gt 3071 ]; then 
    mem=$((phys_mem-1024))
elif [ $phys_mem -gt 2047 ]; then 
    mem=1536
elif [ $phys_mem -gt 1535 ]; then 
    mem=1024
fi

if `java -version 2>&1 | grep -- 64-Bit > /dev/null`; then # We have a 64 bit JVM.
    echo "-Xmx"${mem}M      > "$vm_options_path/Cytoscape.vmoptions"
else # Assume a 32 bit JVM.
    # Truncate memory setting at 1550 MiB:
    if [ $mem -gt 1550 ]; then
        mem=1550
    fi

    echo "-Xmx"${mem}M      > "$vm_options_path/Cytoscape.vmoptions"
fi

exit 0
