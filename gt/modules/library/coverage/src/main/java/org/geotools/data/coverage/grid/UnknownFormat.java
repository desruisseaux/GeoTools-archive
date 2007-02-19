/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.coverage.grid;

import java.util.HashMap;

import org.geotools.factory.Hints;
import org.geotools.image.imageio.GeoToolsWriteParams;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;

/**
 * This class can be used when a proper {@link Format} cannot be found for some
 * input sources.
 * 
 * <p>
 * It implements the abstract method of {@link AbstractGridFormat} but it always
 * returns null to indicate that the format it represents is unknown.
 * 
 * @author Jesse Eichar
 * @author Simone Giannecchini (simboss)
 * @deprecated use
 *             {@link org.geotools.coverage.grid.io.UnknownFormat}
 *             instead.
 */
public class UnknownFormat extends AbstractGridFormat implements Format {
	/**
	 * Creates a new UnknownFormat object.
	 */
	public UnknownFormat() {
		mInfo = new HashMap();
		mInfo.put("name", "Unkown Format");
		mInfo.put("description", "This format describes all unknown formats");
		mInfo.put("vendor", null);
		mInfo.put("docURL", null);
		mInfo.put("version", null);
		readParameters = null;
		writeParameters = null;

	}

	/**
	 * @see AbstractGridFormat#getReader(Object)
	 */
	public GridCoverageReader getReader(java.lang.Object source) {
		return null;
	}

	/**
	 * @see AbstractGridFormat#getWriter(Object)
	 */
	public GridCoverageWriter getWriter(Object destination) {
		throw new UnsupportedOperationException(
				"Trying to get a writer from an unknown format.");
	}




	/**
	 * @see AbstractGridFormat#getReader(Object, Hints)
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		throw new UnsupportedOperationException(
				"Trying to get a reader from an unknown format.");
	}

	/**
	 * @see AbstractGridFormat#getDefaultImageIOWriteParameters()
	 */
	public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
		throw new UnsupportedOperationException(
				"Trying to get a writing parameters from an unknown format.");
	}
	/**
	 * @see AbstractGridFormat#accepts(Object)
	 */
	public  boolean accepts(Object input) {
		return false;
	}

}
