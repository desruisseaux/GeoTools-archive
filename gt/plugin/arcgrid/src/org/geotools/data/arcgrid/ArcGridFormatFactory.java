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
package org.geotools.data.arcgrid;

import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.opengis.coverage.grid.Format;


/**
 * Implementation of the GridCoverageFormat service provider interface for arc grid files.
 *
 * @author aaime
 */
public class ArcGridFormatFactory implements org.geotools.data.GridFormatFactorySpi {
    
	public boolean isAvailable(){
		return true;
	}
    
    /**
     * Returns an instance of a ArcGridFormat.
     * 
     * @return Format used to process ArcGridCoverage files
     */
    public Format createFormat(){
        return new ArcGridFormat();
    }

	/*@todo
	 *  (non-Javadoc)
	 * @see org.geotools.data.GridFormatFactorySpi#createReader(java.lang.Object)
	 */
	public GridCoverageReader createReader(Object source) {
		return new ArcGridReader(source);
	}

	/* @todo
	 * (non-Javadoc)
	 * @see org.geotools.data.GridFormatFactorySpi#createWriter(java.lang.Object)
	 */
	public GridCoverageWriter createWriter(Object destination) {
		return new ArcGridWriter(destination);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.GridFormatFactorySpi#canCreate(org.opengis.coverage.grid.Format)
	 */
	public boolean canCreate(Format format) {
		if( format instanceof ArcGridFormat )
			return true;
		return false;
	}
}
