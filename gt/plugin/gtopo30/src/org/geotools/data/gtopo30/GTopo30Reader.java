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
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;

/**
 * 
 * 
 * @author jeichar
 */
public class GTopo30Reader implements GridCoverageReader {
	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
	 */
	public Format getFormat() {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getSource()
	 */
	public Object getSource() {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataNames()
	 */
	public String[] getMetadataNames() throws IOException {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
	 */
	public String getMetadataValue(String name) throws IOException,
			MetadataNameNotFoundException {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#listSubNames()
	 */
	public String[] listSubNames() throws IOException {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getCurrentSubname()
	 */
	public String getCurrentSubname() throws IOException {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public org.opengis.coverage.grid.GridCoverage read(GeneralParameterValue[] parameters) throws java.lang.IllegalArgumentException, java.io.IOException {
		// @todo Auto-generated method stub
		return null;
	}
	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#dispose()
	 */
	public void dispose() throws IOException {
		// @todo Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
	 */
	public boolean hasMoreGridCoverages() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverageReader#skip()
	 */
	public void skip() throws IOException {
		// TODO Auto-generated method stub
		
	}
}
