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
package org.geotools.gce.gtopo30;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;

/**
 * This class provides a GridCoverageReader for the
 * GTopo30Format.
 *
 * @author jeichar
 * @author mkraemer
 * @source $URL$
 */
public class GTopo30Reader implements GridCoverageReader {
	/**
	 * The GTopo30 format
	 */
    private Format format = new GTopo30FormatFactory().createFormat();
    
    /**
     * The source object that has been passed to our constructor.
     * Can be a file, an URL or a String
     */
    private Object mSource = null;
    
    /**
     * The GTopo30DataStore which will be instantiated by the constructor
     */
    private GTopo30DataSource sourceOfData = null;

    /**
     * GTopo30Reader constructor.
     *
     * @param source The source object (can be a File, an URL or a String representing
     * a File or an URL).
     *
     * @throws MalformedURLException if the URL does not correspond to one of
     *         the GTopo30 files
     * @throws DataSourceException if the given url points to an unrecognized
     *         file
     */
    public GTopo30Reader(final Object source)
        throws MalformedURLException, DataSourceException {
        URL urlToUse = null;

        if (source instanceof GTopo30DataSource) {
            this.sourceOfData = (GTopo30DataSource)source;
        } else {
            if (source instanceof File) {
                urlToUse = ((File)source).toURL();
            } else if (source instanceof URL) {
                //we only allow files
                urlToUse = (URL)source;
            } else if (source instanceof String) {
                try {
                    //is it a filename?
                    urlToUse = new File((String)source).toURL();
                } catch (MalformedURLException e) {
                    //is it a URL
                    urlToUse = new URL((String)source);
                }
            } else {
                throw new IllegalArgumentException("Illegal input argument!");
            }

            this.sourceOfData = new GTopo30DataSource(urlToUse);
        }

        this.mSource = source;
    }
    
    /**
     * Sets an envelope that will be used to crop the source data in order to
     * get fewer data from the file
     *
     * @param crop the rectangle that will be used to extract data from the
     *        file
     */
    public void setCropEnvelope(GeneralEnvelope crop) {
    	this.sourceOfData.setCropEnvelope(crop);
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return this.format;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getSource()
     */
    public Object getSource() {
        return this.mSource;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataNames()
     */
    public String[] getMetadataNames() {
    	throw new UnsupportedOperationException(
        	"GTopo30 reader doesn't support metadata manipulation yet");
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
     */
    public String getMetadataValue(String name) throws MetadataNameNotFoundException {
    	throw new UnsupportedOperationException(
        	"GTopo30 reader doesn't support metadata manipulation yet: " + name);
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#listSubNames()
     */
    public String[] listSubNames() {
        return null;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getCurrentSubname()
     */
    public String getCurrentSubname() {
        return null;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
     */
    public org.opengis.coverage.grid.GridCoverage read(
        GeneralParameterValue[] parameters)
        throws java.lang.IllegalArgumentException, java.io.IOException {
    	if (parameters != null) {
    		//unreferenced parameter: parameters
    	}

        return this.sourceOfData.getGridCoverage();
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#dispose()
     */
    public void dispose() {
        this.sourceOfData = null;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
     */
    public boolean hasMoreGridCoverages() {
        return false;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#skip()
     */
    public void skip() {
    	//nothing to do here
    }
}
