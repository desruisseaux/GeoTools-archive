/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

import javax.imageio.stream.ImageOutputStream;

import org.geotools.factory.Hints;
import org.opengis.coverage.grid.GridCoverageWriter;

/**
 * 
 * 
 * @author Simone Giannecchini
 * @since 2.3.x
 * 
 */
public abstract class AbstractGridCoverageWriter implements GridCoverageWriter {

	/** Hints to be used for the writing process. */
	protected Hints hints = null;

	/** The destination {@link ImageOutputStream}. */
	protected ImageOutputStream outStream = null;

	/**
	 * Default constructor for an {@link AbstractGridCoverageWriter}.
	 */
	public AbstractGridCoverageWriter() {

	}

}
