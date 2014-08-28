package org.cytoscape.launcher.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class SplashPanel extends Component {
	BufferedImage image;
	private Font font;
	Graphics2D context;
	
	public SplashPanel(BufferedImage background) {
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = environment.getDefaultScreenDevice();
		GraphicsConfiguration configuration = device.getDefaultConfiguration();
		image = configuration.createCompatibleImage(background.getWidth(), background.getHeight());
		font = new Font(Font.MONOSPACED,Font.PLAIN,12);
		context = image.createGraphics();
		
		context.fillRect(0, 0, background.getWidth(), background.getHeight());
		context.drawImage(background, 0, 0, null);
        context.setColor(Color.BLACK);
		context.setFont(font);
		context.drawString("Java version: "+System.getProperty("java.version"),650,20);
	}
	
	@Override
	public void paint(Graphics graphics) {
		graphics.drawImage(image, 0, 0, null);
	}
	
	public void updateMessage(final String message, final double progress) {
    	if (!SwingUtilities.isEventDispatchThread()) {
    		try {
    			// Update synchronously, otherwise we end up dropping frames.
    			// In such a case, it appears to the user that little progress
    			// is being made, and then the system suddenly starts.
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						updateMessage(message, progress);
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
    		return;
    	}

    	if (!isDisplayable()) {
    		return;
    	}
    	
        context.setColor(Color.WHITE);
        context.fillRect(20,300,800,40);
        context.setColor(Color.BLACK);
        context.drawString(message, 20, 320);
        
        context.setColor(new Color(computeColor(progress)));
        int progressWidth = (int) (800.0 * progress);
        context.fillRect(20, 304, progressWidth, 4);
        
        repaint();
	}
	
	private int computeColor(double progress) {
		int red = (int) (progress * 247);
		int green = (int) (progress * 148);
		int blue = (int) (progress * 30);
		return (red << 16) | (green << 8) | blue;
	}

	public void close() {
		Container parent = getParent();
		while (parent != null) {
			// Find the enclosing JFrame/JDialog and try to close it.
			if (parent instanceof Window) {
				parent.setVisible(false);
			}
			parent = parent.getParent();
		}
	}
}
