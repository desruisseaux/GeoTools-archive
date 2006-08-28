/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    Created on April 17, 2004, 1:52 PM
 */
package org.geotools.renderer.lite;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.batik.transcoder.TranscoderInput;

/**
 * Turns SVG vector drawings into buffered images geotools can use for rendering
 * 
 * @author James
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/render/src/org/geotools/renderer/lite/SVGGlyphRenderer.java $
 */
public class SVGGlyphRenderer implements GlyphRenderer {

	private static final java.util.List formats = java.util.Collections
			.unmodifiableList(java.util.Arrays
					.asList(new String[] { "image/svg" }));

	/** The logger for the rendering module. */
	private static final Logger LOGGER = Logger
			.getLogger("org.geotools.rendering");

	// static {
	// do register our xml reader wrapper against batik so that we can use
	// jaxp instead of the hard-coded xerces implementation
	// XMLResourceDescriptor.setXMLParserClassName(BatikXMLReader.class.getName());
	// }

	/** Creates a new instance of SVGGlyphRenderer */
	public SVGGlyphRenderer() {
	}

	public boolean canRender(String format) {
		return (format.toLowerCase().equals("image/svg+xml"));
	}

	public java.util.List getFormats() {
		return formats;
	}

	public java.awt.image.BufferedImage render(
			org.geotools.styling.Graphic graphic,
			org.geotools.styling.ExternalGraphic eg,
			org.geotools.feature.Feature feature) {
		try {
			URL svgfile = eg.getLocation();
			InternalTranscoder magic = new InternalTranscoder();
			TranscoderInput in = new TranscoderInput(svgfile.openStream());
			magic.transcode(in, null);
			return magic.getImage();
		} catch (java.io.IOException mue) {
			LOGGER.log(Level.WARNING, "Unable to load external svg file", mue);
			return null;
		} catch (Exception te) {
			LOGGER.log(Level.WARNING, "Unable to load external svg file", te);
			return null;
		}
	}

}
