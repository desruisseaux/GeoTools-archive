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
package org.geotools.data.gtopo30;

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


/**
 * DOCUMENT ME!
 *
 * @author giannecchini TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class GTopo30Format extends AbstractGridFormat implements Format {
    /**
     *
     */
    private GTopo30DataSource source = null;
    private Object input = null;

    /**
     * Creates an instance and sets the metadata.
     */
    public GTopo30Format() {
        setInfo();
    }

    /**
     * Sets the metadata information.
     */
    private void setInfo() {
        HashMap info = new HashMap();

        info.put("name", "Gtopo30");
        info.put("description", "Gtopo30 Coverage Format");
        info.put("vendor", "Geotools");
        info.put("docURL", "http://edcdaac.usgs.gov/gtopo30/gtopo30.asp");
        info.put("version", "1.0");
        mInfo = info;

        //reading parameters
        readParameters = null;

        //reading parameters
        writeParameters = null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#getReader(java.lang.Object)
     */
    public GridCoverageReader getReader(Object source) {
        // TODO Auto-generated method stub
        try {
            if ((this.input != null) && this.input.equals(source)) {
                return new GTopo30Reader(this.source);
            } else {
                return new GTopo30Reader(source);
            }
        } catch (Exception e) {
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#getWriter(java.lang.Object)
     */
    public GridCoverageWriter getWriter(Object destination) {
        return new GTopo30Writer(destination);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#accepts(java.lang.Object)
     */
    public boolean accepts(Object input) {
        this.input = input;

        //trying to read the header and statistics
        source = null;

        URL urlToUse = null;

        if (input instanceof File) {
            try {
                urlToUse = ((File) input).toURL();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                urlToUse = null;

                return false;
            }
        } else if (input instanceof URL) {
            //we only allow files
            urlToUse = (URL) input;
        } else if (input instanceof String) {
            try {
                //is it a filename?
                urlToUse = new File((String) input).toURL();
            } catch (MalformedURLException e) {
                //is it a URL
                try {
                    urlToUse = new URL((String) input);
                } catch (MalformedURLException e1) {
                    urlToUse = null;

                    return false;
                }
            }
        }
        else {
            return false;
        }

        try {
            source = new GTopo30DataSource(urlToUse);

            //        //well we have an url, let's try to use it!!!
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return false;
        }

        return true;
    }
}
