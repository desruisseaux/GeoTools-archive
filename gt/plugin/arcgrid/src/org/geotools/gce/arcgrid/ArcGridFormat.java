/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, 2004 Geotools Project Managment Committee (PMC)
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
package org.geotools.gce.arcgrid;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.parameter.ParameterDescriptor;
import org.geotools.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.GeneralParameterDescriptor;

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
        
        
        readParameters = new ParameterDescriptorGroup( mInfo, new GeneralParameterDescriptor[]{ GRASS, COMPRESS } );        
        writeParameters = new ParameterDescriptorGroup( mInfo, new GeneralParameterDescriptor[]{ GRASS, COMPRESS} );        
    }
    
    /** Indicates whether the arcgrid data is compressed with GZIP */
    public static final ParameterDescriptor COMPRESS = new ParameterDescriptor( "Compressed", "Indicates whether the arcgrid data is compressed with GZIP", Boolean.FALSE, true );
    
    /** Indicates whether the arcgrid is in GRASS format */
    public static final ParameterDescriptor GRASS = new ParameterDescriptor( "GRASS", "Indicates whether arcgrid is in GRASS format", Boolean.FALSE, true );
    
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
        
        String pathname = null;
        if (input instanceof String) {
            pathname = (new File((String)input)).getName() ; 
        }
        if (input instanceof File) {
            pathname = ((File)input).getName();
        }
        if (input instanceof URL) {
            URL url = (URL) input;
            pathname = url.getFile();
        }

        if (pathname != null && 
            (pathname.endsWith(".asc") || pathname.endsWith(".asc.gz")) ||
            (pathname.endsWith(".ASC") || pathname.endsWith(".ASC.GZ"))) {
             return true;
        } else {
            return false;
        }
    }

}