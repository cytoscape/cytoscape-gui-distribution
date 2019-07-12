package org.cytoscape.launcher.internal;

import java.awt.Desktop;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.OpenFilesEvent;

public class MacHelper {

    public static void handleStartupArguments() {
        // All Mac EAWT logic
		// Intercept session file double-clicked on Mac OS, passed by file association set by install4j
		Desktop desktop = Desktop.getDesktop();
		
		desktop.setOpenFileHandler(new OpenFilesHandler() {

			@Override
			public void openFiles(OpenFilesEvent arg0) {
				if (Launcher.startupArguments.length > 0 || arg0.getFiles().size()>1){
					return;
				}
				
				String fileName = arg0.getFiles().get(0).getAbsolutePath();
				if (!fileName.endsWith(".cys")){
					return;
				}
				
				String[] argsArray = {fileName};
				Launcher.startupArguments = argsArray;	
			}			
		});
    }
}
