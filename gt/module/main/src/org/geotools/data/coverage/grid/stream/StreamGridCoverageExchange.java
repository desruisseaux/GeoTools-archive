/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.coverage.grid.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;


import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageExchange;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;


import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.coverage.grid.AbstractGridFormat;


/**
 * A simple stateless GridCoverageExchange that will write/read to/from files and streams
 *
 * @author jeichar
 * @author <a href="mailto:simboss_ml@tiscali.it">Simone Giannecchini (simboss)</a>
 * @source $URL$
 */
public class StreamGridCoverageExchange implements GridCoverageExchange {
    Set formats=new java.util.HashSet();

    public StreamGridCoverageExchange() {
        for (Iterator iter = GridFormatFinder.getAvailableFormats(); iter.hasNext();) {
            GridFormatFactorySpi factory = (GridFormatFactorySpi) iter.next();
            formats.add(factory.createFormat());
        }
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

        return ((AbstractGridFormat)format).getWriter(destination);
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
        Format[] f=new Format[formats.size()];
        formats.toArray(f);
        return f;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageExchange#getReader(java.lang.Object)
     */
    public GridCoverageReader getReader(Object arg0) throws IOException {
        for (Iterator iter = formats.iterator(); iter.hasNext();) {
            Format f = (Format) iter.next();
            if( ((AbstractGridFormat)f).accepts(arg0))
                return ((AbstractGridFormat)f).getReader(arg0);
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

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageExchange#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * @see org.geotools.data.coverage.grid.GridCoverageExchange#accepts(java.net.URL)
     */
    public boolean setDataSource(Object datasource) {
        return isLegalSource(datasource);
    }
}
