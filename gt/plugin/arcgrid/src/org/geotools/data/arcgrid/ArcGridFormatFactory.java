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

import java.net.URL;

import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;


/**
 * Implementation of the GridCoverageFormat service provider interface for arc grid files.
 *
 * @author aaime
 */
public class ArcGridFormatFactory
    implements GridFormatFactorySpi {
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * Returns an instance of a ArcGridFormat.
     *
     * @return Format used to process ArcGridCoverage files
     */
    public Format createFormat() {
        return new ArcGridFormat();
    }

    /**
     * @see org.geotools.data.GridFormatFactorySpi#createReader(java.lang.Object)
     */
    public GridCoverageReader createReader(Object source) {
        return new ArcGridReader(source);
    }

    /**
     * @see org.geotools.data.GridFormatFactorySpi#createWriter(java.lang.Object)
     */
    public GridCoverageWriter createWriter(Object destination) {
        return new ArcGridWriter(destination);
    }

    /** 
     * @see org.geotools.data.GridFormatFactorySpi#accepts(java.net.URL)
     */
    public boolean accepts(URL input) {
        String pathname=input.getFile();
        if( pathname.endsWith(".asc") )
            return true;
        return false;
    }
}
