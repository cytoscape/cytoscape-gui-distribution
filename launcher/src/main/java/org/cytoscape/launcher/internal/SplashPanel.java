package org.cytoscape.launcher.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class SplashPanel extends Component {
	
	private final int BORDER_WIDTH = 4;
	private final int PROGRESS_BAR_HEIGHT = 4;
	private final int PAD = 20;
	private final Color THEME_COLOR = new Color(234, 145, 35); // "CyColor.primary"
	private final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	
	BufferedImage image;
	Graphics2D context;
	
	private final int fontHeight;
	
	public SplashPanel(BufferedImage background) {
		int imgWidth = background.getWidth();
		int imgHeight = background.getHeight();
		
		Dimension bounds = new Dimension(imgWidth + 2 * BORDER_WIDTH, imgHeight + 2 * BORDER_WIDTH);
		setMinimumSize(bounds);
		setPreferredSize(bounds);
		setMaximumSize(bounds);
		
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = environment.getDefaultScreenDevice();
		GraphicsConfiguration configuration = device.getDefaultConfiguration();
		image = configuration.createCompatibleImage(imgWidth, imgHeight);
		
		context = image.createGraphics();
		context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		context.fillRect(0, 0, imgWidth, imgHeight);
		context.drawImage(background, 0, 0, null);
		
		JLabel lbl = new JLabel("Java Version: " + System.getProperty("java.version"));
		FontMetrics fontMetrics = lbl.getFontMetrics(FONT);
		fontHeight = fontMetrics.getHeight();
		int fontWidth = lbl.getPreferredSize().width;
		
		context.setColor(THEME_COLOR);
		context.setFont(FONT);
		context.drawString(lbl.getText(), imgWidth - PAD - fontWidth, PAD + fontHeight);
		
		drawProgressString("Initializing OSGi container...");
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(THEME_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(image, BORDER_WIDTH, BORDER_WIDTH, image.getWidth(), image.getHeight(), null);
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

    	if (!isDisplayable())
    		return;
    	
		drawProgressString(message);
		
		final int w = image.getWidth();
		final int h = image.getHeight();
		final int th = fontHeight; // Text Height
		final int progressWidth = (int) ((w - 2 * PAD) * progress);

		context.setColor(new Color(computeColor(progress)));
		context.fillRect(PAD, h - PAD - th, progressWidth, PROGRESS_BAR_HEIGHT);
        
        repaint();
	}
	
	public void close() {
		Container parent = getParent();
		
		while (parent != null) {
			// Find the enclosing JFrame/JDialog and try to close it.
			if (parent instanceof Window)
				parent.setVisible(false);
			
			parent = parent.getParent();
		}
	}
	
	private void drawProgressString(final String s) {
		final int w = image.getWidth();
		final int h = image.getHeight();
		final int th = fontHeight; // Text Height
		final int tw = w - 2 * PAD; // Text Width
    	
		context.setColor(Color.WHITE);
		context.fillRect(PAD - 2, h - PAD - th - 2, tw + 4, th + PAD + 4);
		context.setColor(Color.BLACK);
		context.drawString(s, PAD, h - PAD);
	}
	
	private int computeColor(double progress) {
		int red = (int) (progress * 247);
		int green = (int) (progress * 148);
		int blue = (int) (progress * 30);
		return (red << 16) | (green << 8) | blue;
	}
}
