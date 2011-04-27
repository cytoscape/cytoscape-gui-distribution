#!/bin/bash

java -jar -Xss10M -Xmx1550M -Dawt.useSystemAAFontSettings=lcd -Dapple.laf.useScreenMenuBar=true -Dapple.awt.rendering=speed -Dapple.awt.fileDialogForDirectories=true -Dcom.apple.mrj.application.apple.menu.about.name="Cytoscape 3" -Dorg.ops4j.pax.logging.DefaultServiceLog.level=NONE -Dbundles.configuration.location=bundles/configurations -Dfelix.fileinstall.dir=bundles/plugins cytoscape-launcher.jar 
