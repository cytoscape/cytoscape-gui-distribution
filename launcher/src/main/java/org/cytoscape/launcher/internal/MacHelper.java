package org.cytoscape.launcher.internal;

import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.AppEvent.OpenFilesEvent;


public class MacHelper {

    public static void handleStartupArguments() {
        // All Mac EAWT logic
		// Intercept session file double-clicked on Mac OS, passed by file association set by install4j
		Application application = Application.getApplication();
		
		application.setOpenFileHandler(new OpenFilesHandler() {
			@Override
			public void openFiles(OpenFilesEvent e) {
				
				if (Launcher.startupArguments.length > 0 || e.getFiles().size()>1){
					return;
				}
				
				String fileName = e.getFiles().get(0).getAbsolutePath();
				if (!fileName.endsWith(".cys")){
					return;
				}
				
				String[] argsArray = {fileName};
				Launcher.startupArguments = argsArray;				
			}			
		});
    }
}
