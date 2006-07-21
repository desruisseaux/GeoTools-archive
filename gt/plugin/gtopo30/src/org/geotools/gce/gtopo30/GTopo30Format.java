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
/*
 * Created on Apr 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.gtopo30;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * Provides basic information about the GeoTIFF format IO.
 * 
 * @author Simone Giannecchini
 * @author mkraemer
 * @source $URL$
 */
public final class GTopo30Format extends AbstractGridFormat implements Format {
	/**
	 * Creates an instance and sets the metadata.
	 */
	public GTopo30Format() {
		mInfo = new HashMap();
		mInfo.put("name", "Gtopo30");
		mInfo.put("description", "Gtopo30 Coverage Format");
		mInfo.put("vendor", "Geotools");
		mInfo.put("docURL", "http://edcdaac.usgs.gov/gtopo30/gtopo30.asp");
		mInfo.put("version", "1.0");

		// reading parameters
		readParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(
						mInfo,
						new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D }));

		// reading parameters
		writeParameters = null;
	}

	/**
	 * Returns a reader object which you can use to read GridCoverages from a
	 * given source
	 * 
	 * @param o
	 *            the the source object. This can be a File, an URL or a String
	 *            (representing a filename or an URL)
	 * 
	 * @return a GridCoverageReader object or null if the source object could
	 *         not be accessed.
	 */
	public GridCoverageReader getReader(final Object o) {
		try {
			return new GTopo30Reader(o);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns a writer object which you can use to write GridCoverages to a
	 * given destination.
	 * 
	 * @param destination
	 *            The destination object
	 * 
	 * @return a GridCoverageWriter object
	 */
	public GridCoverageWriter getWriter(final Object destination) {
		return new GTopo30Writer(destination);
	}

	/**
	 * Checks if the GTopo30DataSource supports a given file
	 * 
	 * @param o
	 *            the source object to test for compatibility with this format.
	 *            This can be a File, an URL or a String (representing a
	 *            filename or an URL)
	 * 
	 * @return if the source object is compatible
	 */
	public boolean accepts(final Object o) {
		URL urlToUse;

		if (o instanceof File) {
			try {
				urlToUse = ((File) o).toURL();
			} catch (MalformedURLException e) {
				return false;
			}
		} else if (o instanceof URL) {
			// we only allow files
			urlToUse = (URL) o;
		} else if (o instanceof String) {
			try {
				// is it a filename?
				urlToUse = new File((String) o).toURL();
			} catch (MalformedURLException e) {
				// is it a URL
				try {
					urlToUse = new URL((String) o);
				} catch (MalformedURLException e1) {
					return false;
				}
			}
		} else {
			return false;
		}

		// trying to create a reader
		try {
			final GTopo30Reader reader = new GTopo30Reader(urlToUse);
		} catch (IOException e) {
			return false;
		}

		return true;
	}
}
