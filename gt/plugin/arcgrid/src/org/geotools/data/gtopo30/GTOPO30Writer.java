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
 * 	  Created: Apr 30, 2004
 *
 */
package org.geotools.data.gtopo30;

import java.io.IOException;

import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;

/**
 * 
 * 
 * @author jeichar
 */
public class GTOPO30Writer implements GridCoverageWriter {
	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getFormat()
	 */
	public Format getFormat() {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getDestination()
	 */
	public Object getDestination() {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getMetadataNames()
	 */
	public String[] getMetadataNames() {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String, java.lang.String)
	 */
	public void setMetadataValue(String name, String value) throws IOException,
			MetadataNameNotFoundException {
		// @todo Auto-generated method stub
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
	 */
	public void setCurrentSubname(String name) throws IOException {
		// @todo Auto-generated method stub
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage, org.opengis.parameter.GeneralParameterValue[])
	 */
	public void write(GridCoverage coverage, GeneralParameterValue[] parameters)
			throws InvalidParameterNameException,
			InvalidParameterValueException, ParameterNotFoundException,
			IOException {
		// @todo Auto-generated method stub
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
	 */
	public void dispose() throws IOException {
		// @todo Auto-generated method stub
	}
}
