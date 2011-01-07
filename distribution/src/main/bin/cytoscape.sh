#!/bin/bash

java -jar -Xss10M -Xmx1550M -Dswing.aatext=true -Dawt.useSystemAAFontSettings=lcd -Dapple.laf.useScreenMenuBar=true -Dapple.awt.fileDialogForDirectories=true -Dapple.awt.graphics.UseQuartz=true -Dcom.apple.mrj.application.apple.menu.about.name=Cytoscape -Dorg.ops4j.pax.logging.DefaultServiceLog.level=NONE -Dbundles.configuration.location=bundles/configurations launcher-1.0-SNAPSHOT-jar-with-dependencies.jar 
