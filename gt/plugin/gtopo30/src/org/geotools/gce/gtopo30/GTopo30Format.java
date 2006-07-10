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
package org.geotools.gce.gtopo30;

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Provides basic information about the GeoTIFF format IO.
 * 
 * @author giannecchini
 * @author mkraemer
 * @source $URL$
 */
public class GTopo30Format extends AbstractGridFormat implements Format {
    /**
     * Creates an instance and sets the metadata.
     */
    public GTopo30Format() {
    	this.mInfo = new HashMap();
    	this.mInfo.put("name", "Gtopo30");
    	this.mInfo.put("description", "Gtopo30 Coverage Format");
    	this.mInfo.put("vendor", "Geotools");
    	this.mInfo.put("docURL", "http://edcdaac.usgs.gov/gtopo30/gtopo30.asp");
    	this.mInfo.put("version", "1.0");

    	//reading parameters
        this.readParameters = null;

        //reading parameters
        this.writeParameters = null;
    }

    /**
     * Returns a reader object which you can use to read GridCoverages from
     * a given source
     * 
     * @param o the the source object. This can be a File, an URL or a
     *          String (representing a filename or an URL)
     *          
     * @return a GridCoverageReader object or null if the source object
     *           could not be accessed.
     */
    public GridCoverageReader getReader(Object o) {
        try {
            return new GTopo30Reader(o);
        } catch (Exception e) {
        	return null;
        }
    }

    /**
     * Returns a writer object which you can use to write GridCoverages to
     * a given destination.
     * 
     * @param destination The destination object
     * 
     * @return a GridCoverageWriter object
     */
    public GridCoverageWriter getWriter(Object destination) {
        return new GTopo30Writer(destination);
    }

    /**
     * Checks if the GTopo30DataSource supports a given file
     * 
     * @param o the source object to test for compatibility with this format.
     *          This can be a File, an URL or a String (representing a filename
     *          or an URL)
     * 
     * @return if the source object is compatible
     */
    public boolean accepts(Object o) {
        URL urlToUse = null;

        if (o instanceof File) {
            try {
                urlToUse = ((File)o).toURL();
            } catch (MalformedURLException e) {
                return false;
            }
        } else if (o instanceof URL) {
            //we only allow files
            urlToUse = (URL)o;
        } else if (o instanceof String) {
            try {
                //is it a filename?
                urlToUse = new File((String)o).toURL();
            } catch (MalformedURLException e) {
                //is it a URL
                try {
                    urlToUse = new URL((String) o);
                } catch (MalformedURLException e1) {
                    return false;
                }
            }
        }
        else {
            return false;
        }

        //trying to read the header and statisticss
        try {
            GTopo30DataSource source = new GTopo30DataSource(urlToUse);
            source.getBounds();
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
