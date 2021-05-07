package org.cytoscape.launcher.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

/*
 * #%L
 * Cytoscape Launcher
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public class SplashPanel extends Component {
	
	/**
	 * We expect the background image to be larger than the splash screen, so it does not look pixelated
	 * on high density monitors. This is the expected scale.
	 */
	private static final int BORDER_WIDTH = 1;
	private static final int PROGRESS_BAR_HEIGHT = 4;
	private static final int PAD = 20;
	private static final Color BORDER_COLOR = new Color(42, 42, 42);
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	
	private SVGDiagram svgDiagram;
	
	private final int fontHeight;
	private final int fontWidth;
	private final String javaInfo = "Java " + System.getProperty("java.version");
	
	private String message;
	private double progress;
	
	public SplashPanel(String svg) {
		svgDiagram = createSVGDiagram(svg);
		
		int w = Math.round(svgDiagram.getWidth());
		int h = Math.round(svgDiagram.getHeight());
		
		var bounds = new Dimension(w + 2 * BORDER_WIDTH, h + 2 * BORDER_WIDTH);
		setMinimumSize(bounds);
		setPreferredSize(bounds);
		setMaximumSize(bounds);
		
		var lbl = new JLabel(javaInfo);
		var fontMetrics = lbl.getFontMetrics(FONT);
		fontHeight = fontMetrics.getHeight();
		fontWidth = lbl.getPreferredSize().width;
		
		updateMessage("Initializing OSGi container...", 0.0);
	}
	
	@Override
	public void paint(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		
		// Draw background rectangle
		g.setColor(BORDER_COLOR);
		g.fillRect(0, 0, w, h);
		
		// Draw background image
		{
			var g2 = createGraphics2D(g);
			g2.translate(BORDER_WIDTH, BORDER_WIDTH);
			
			try {
				svgDiagram.render(g2);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			g2.dispose();
		}
		// Draw the Java info
		{
			var g2 = createGraphics2D(g);

			g2.setColor(BORDER_COLOR);
			g2.fillRect(0, h - PAD - fontHeight - 4, w, PROGRESS_BAR_HEIGHT);

			g2.setColor(TEXT_COLOR);
			g2.setFont(FONT);
			g2.drawString(javaInfo, w - PAD - fontWidth, PAD + fontHeight);

			g2.dispose();
		}
		
		if (message != null)
			drawProgressString(g);
		
		if (progress > 0)
			drawProgressBar(g);
	}

	public void updateMessage(String message, double progress) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				// Update synchronously, otherwise we end up dropping frames.
				// In such a case, it appears to the user that little progress
				// is being made, and then the system suddenly starts.
				SwingUtilities.invokeAndWait(() -> updateMessage(message, progress));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			return;
		}

		this.message = message;
		this.progress = progress;
		
		if (isDisplayable())
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
	
	private SVGDiagram createSVGDiagram(String svg) {
		var universe = new SVGUniverse();
		var is = new StringReader(svg);
		var uri = universe.loadSVG(is, "bg");
		var diagram = universe.getDiagram(uri);
		diagram.setIgnoringClipHeuristic(true);
		
		return diagram;
	}
	
	private void drawProgressString(Graphics g) {
		int h = getHeight();

		var g2 = createGraphics2D(g);
		
		g2.setColor(TEXT_COLOR);
		g2.drawString(message, PAD, h - PAD);
		
		g2.dispose();
	}
	
	private void drawProgressBar(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		int th = fontHeight; // Text Height
		int progressWidth = (int) (w * progress);

		var g2 = createGraphics2D(g);
		
		g2.setColor(TEXT_COLOR);
		g2.fillRect(0, h - PAD - th - 4, progressWidth, PROGRESS_BAR_HEIGHT);
		
		g2.dispose();
	}
	
	private Graphics2D createGraphics2D(Graphics g) {
		var g2 = (Graphics2D) g.create();
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		return g2;
	}
}
