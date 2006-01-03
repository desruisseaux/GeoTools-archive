/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */

package org.geotools.renderer.lite;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.media.jai.util.Range;

import org.geotools.feature.Feature;
import org.geotools.renderer.style.TextStyle2D;
import org.geotools.styling.TextSymbolizer;

/**
 * An interface for a label cache. 
 * 
 * @author jeichar
 * @since 0.9.0
 */
public interface LabelCache {
	/**
	 * Called by renderer to indicate that the rendering process is starting.
	 */
	void start();
	/**
	 * Called by renderer to indication the start of rendering a layer.
	 */
	void startLayer();
	/**
	 * Puts a TextStyle and its associated shape in the cache. 
	 * @param textStyle The TextStyle2D containing the style information 
	 * @param shape the shape to be labeled
	 */
	void put(TextSymbolizer symbolizer, Feature feature, LiteShape2 shape, Range scaleRange) ;
	/**
	 * Called to indicate that a layer is done rendering.  The method may draw labels if appropriate
	 * for the labeling algorithm 
	 * 
	 * @param graphics the graphics to draw on.
	 * @param displayArea The size of the display area.
	 */
	void endLayer(Graphics2D graphics, Rectangle displayArea);
	/**
	 * Called to indicate that the map is done rendering.  The method may draw labels if appropriate
	 * for the labeling algorithm 
	 * 
	 * @param graphics the graphics to draw on.
	 * @param displayArea The size of the display area.
	 */
	void end(Graphics2D graphics, Rectangle displayArea);
	
	/**
	 * Tells the cache to stop labelling.
	 */
	void stop();
}
