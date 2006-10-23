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
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/main/src/org/geotools/data/coverage/grid/UnknownFormat.java $
 * @version $Revision: 1.9 $
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
		return null;
	}

	/**
	 * @see AbstractGridFormat#accepts(Object)
	 */
	public boolean accepts(Object input) {
		return false;
	}

	/**
	 * @see AbstractGridFormat#getReader(Object, Hints)
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		return null;
	}

}
