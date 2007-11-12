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
package org.geotools.gce.mrsid;

import it.geosolutions.imageio.plugins.mrsid.MrSIDImageReaderSpi;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;

/**
 * Implementation of the {@link Format} service provider interface for MrSID
 * files.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public final class MrSIDFormatFactory implements GridFormatFactorySpi {
	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.gce.mrsid");

	/**
	 * Tells me if the coverage plugin to access MrSID is availaible or not.
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
			
			Class.forName("it.geosolutions.imageio.plugins.mrsid.MrSIDImageReaderSpi");
			available = MrSIDImageReaderSpi.isAvailable();
			MrSIDImageReaderSpi spi = new MrSIDImageReaderSpi();
			available = available && spi.isDriverAvailable();
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("MrSIDFormatFactory is availaible.");
		} catch (ClassNotFoundException cnf) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("MrSIDFormatFactory is not availaible.");
			available = false;
		}
		return available;
	}

	/**
	 * Creating a {@link MrSIDFormat}.
	 * 
	 * @return A {@link MrSIDFormat}.;
	 */
	public Format createFormat() {
		return new MrSIDFormat();
	}

	/**
	 * Returns the implementation hints. The default implementation returns en
	 * empty map.
	 */
	public Map getImplementationHints() {
		return Collections.EMPTY_MAP;
	}
}
