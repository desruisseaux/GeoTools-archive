/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *	  (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.gce.ecw;

import it.geosolutions.imageio.plugins.ecw.ECWImageReaderSpi;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;

/**
 * Implementation of the {@link Format} service provider interface for ECW
 * files.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public final class ECWFormatFactory implements GridFormatFactorySpi {
	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.gce.ecw");

	/**
	 * Tells me if the coverage plugin to access ECW is availaible or not.
	 * 
	 * @return True if the plugin is availaible, False otherwise.
	 */
	public boolean isAvailable() {
		boolean available = true;

		// if these classes are here, then the runtine environment has
		// access to JAI and the JAI ImageI/O toolbox.
		try {

			Class.forName("javax.media.jai.JAI");
			Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
			
			Class.forName("it.geosolutions.imageio.plugins.ecw.ECWImageReaderSpi");
			available = ECWImageReaderSpi.isAvailable();
			ECWImageReaderSpi spi = new ECWImageReaderSpi();
			available = available && spi.isDriverAvailable();
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("ECWFormatFactory is availaible.");
		} catch (ClassNotFoundException cnf) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("ECWFormatFactory is not availaible.");
			available = false;
		}

		return available;
	}

	/**
	 * Creating a {@link ECWFormat}.
	 * 
	 * @return A {@link ECWFormat}.;
	 */
	public Format createFormat() {
		return new ECWFormat();
	}

	/**
	 * Returns the implementation hints. The default implementation returns en
	 * empty map.
	 * 
	 * @return DOCUMENT ME!
	 */
	public Map getImplementationHints() {
		return Collections.EMPTY_MAP;
	}
}
