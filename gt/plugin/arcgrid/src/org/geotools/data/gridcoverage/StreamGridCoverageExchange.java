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
 * Created on Apr 23, 2004
 */
package org.geotools.data.gridcoverage;

import org.geotools.data.arcgrid.ArcGridFormatFactory;
import org.geotools.data.arcgrid.ArcGridReader;
import org.geotools.data.arcgrid.ArcGridWriter;
import org.geotools.gc.exchange.*;
import org.opengis.coverage.grid.Format;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;


/**
 * A simple stateless GridCoverageExchange that will write/read to/from files and streams
 *
 * @author jeichar
 */
public class StreamGridCoverageExchange implements GridCoverageExchange {
    Format[] mFormats = new Format[1];

    public StreamGridCoverageExchange() {
        mFormats[0] = (new ArcGridFormatFactory()).create();
    }

    /**
     * Checks the source Object and ensures that it is legal for this type of
     * GridCoverageExchange
     * This method is basic and can easily be fooled at this point
     *
     * @param source the source object to check
     *
     * @return
     *      true if source is an object understood by StreamGridCoverageExchange
     *
     * @see org.opengis.coverage.grid.GridCoverageExchange#getReader(java.lang.Object,
     *      org.opengis.coverage.grid.Format)
     */
    public boolean isLegalSource(Object source) {
        if (source instanceof InputStream) {
            return true;
        }

        if (source instanceof String) {
            return true;
        }

        if (source instanceof URL) {
            return true;
        }

        if (source instanceof File) {
            return true;
        }

        if (source instanceof Reader) {
            return true;
        }

        return false;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageExchange#getWriter(java.lang.Object,
     *      org.opengis.coverage.grid.Format)
     */
    public GridCoverageWriter getWriter(Object destination, Format format)
        throws IOException {
        if (format.getName().equals("ArcGrid")) {
            return new ArcGridWriter(destination);
        }

        return null;
    }

    /**
     * Checks the source Object and ensures that it is legal for this type of
     * GridCoverageExchange
     * 
     * This method is basic and can easily be fooled at this point
     * 
     * @param destination the source object to check
     *
     * @return 
     *      true if destination is an object understood by StreamGridCoverageExchange
     *
     * @see org.opengis.coverage.grid.GridCoverageExchange#getReader(java.lang.Object,
     *      org.opengis.coverage.grid.Format)
     */
    public boolean isLegalDestination(Object destination) {
        if (destination instanceof OutputStream) {
            return true;
        }

        if (destination instanceof String) {
            return true;
        }

        if (destination instanceof URL) {
            return true;
        }

        if (destination instanceof File) {
            return true;
        }

        if (destination instanceof Writer) {
            return true;
        }

        return false;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageExchange#getFormats()
     */
    public Format[] getFormats() {
        return mFormats;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageExchange#getReader(java.lang.Object)
     */
    public GridCoverageReader getReader(Object arg0) throws IOException {
        if (!isLegalSource(arg0)) {
            throw new IOException(
                "source is not a recognized legal source for this type of GridCoverageExchange");
        }

        switch (IOExchange.determineSourceFormat(arg0)) {
        case 0:
            return new ArcGridReader(arg0);
        }

        return null;
    }

    /**
     * This is a stateless GridCoverageExchange therefore nothing needs to be
     * done
     *
     * @see org.opengis.coverage.grid.GridCoverageExchange#dispose()
     */
    public void dispose() throws IOException {
    }
}
