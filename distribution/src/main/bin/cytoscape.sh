#!/bin/bash

CY3_OPTIONS="-Xss10M -Xmx1550M -Dawt.useSystemAAFontSettings=lcd -Dapple.laf.useScreenMenuBar=true -Dapple.awt.rendering=speed -Dapple.awt.fileDialogForDirectories=true -Dcom.apple.mrj.application.apple.menu.about.name="Cytoscape 3" -Dorg.ops4j.pax.logging.DefaultServiceLog.level=NONE -Dbundles.configuration.location=bundles/configurations -Dfelix.fileinstall.dir=bundles/plugins"

java $CY3_OPTIONS -jar cytoscape-launcher.jar 

# use this instead with a java debugger
#java $CY3_OPTIONS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=12345 -jar cytoscape-launcher.jar