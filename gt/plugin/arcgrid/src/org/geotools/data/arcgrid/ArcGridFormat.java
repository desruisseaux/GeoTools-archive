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

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.parameter.Parameter;
import org.geotools.parameter.ParameterDescriptor;
import org.geotools.parameter.ParameterGroup;
import org.opengis.parameter.GeneralParameterValue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

/**
 * A simple implementation of the Arc Grid Format Metadata
 * 
 * @author jeichar
 */
public class ArcGridFormat extends AbstractGridFormat {
    /**
     * creates an instance and sets the metadata
     */
    public ArcGridFormat() {
        setInfo();
    }

    /**
     * sets the metadata information
     *  
     */
    private void setInfo() {
        HashMap info = new HashMap();
        info.put("name", "ArcGrid");
        info.put("description", "Arc Grid Coverage Format");
        info.put("vendor", "Geotools");
        info.put("docURL", "http://gdal.velocet.ca/projects/aigrid/index.html");
        info.put("version", "1.0");
        mInfo = info;
        
        readParameters = new ParameterGroup( mInfo,
                new GeneralParameterValue[]{ getGRASSParam(), getCompressParam()} );
        
        writeParameters = new ParameterGroup( mInfo,
                new GeneralParameterValue[]{ getGRASSParam(), getCompressParam()} );        
    }
    /**
     * Creates a "Compress" Parameter.  Indicates whether the arcgrid data is
     * compressed with GZIP
     *
     * @return a "Compress" Parameter.
     */
    public static Parameter getCompressParam() {
        return new Parameter(new ParameterDescriptor( "Compressed", "Indicates whether the arcgrid data is compressed with GZIP", Boolean.FALSE, true ));        
    }
    /**
     * Creates a "GRASS" Parameter.  Indicates whether the arcgrid is in GRASS
     * format
     *
     * @return a "GRASS" Parameter.
     */
    public static Parameter getGRASSParam() {
        return new Parameter(new ParameterDescriptor( "GRASS", "Indicates whether arcgrid is in GRASS format", Boolean.FALSE, true ));
    }

    /**
     * @see org.geotools.data.GridFormatFactorySpi#createReader(java.lang.Object)
     */
    public GridCoverageReader getReader(Object source) {
        return new ArcGridReader(source);
    }

    /**
     * @see org.geotools.data.GridFormatFactorySpi#createWriter(java.lang.Object)
     */
    public GridCoverageWriter getWriter(Object destination) {
        return new ArcGridWriter(destination);
    }

    /**
     * Returns an instance of a ArcGridFormat.
     * 
     * @return Format used to process ArcGridCoverage files
     */
    /**
     * @see org.geotools.data.GridFormatFactorySpi#accepts(java.net.URL)
     */
    public boolean accepts(Object input) {
        if (input instanceof File) {
            File f = (File) input;
            if (f.getName().endsWith(".asc"))
                return true;
        }
        if (input instanceof URL) {
            URL url = (URL) input;
            String pathname = url.getFile();
            if (pathname.endsWith(".asc"))
                return true;
        }
        return false;
    }

}