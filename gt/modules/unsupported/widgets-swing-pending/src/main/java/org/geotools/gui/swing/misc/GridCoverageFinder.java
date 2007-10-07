/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.misc;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.coverage.grid.GridCoverage;

/**
 * Static class to build GridCoverage
 * @author johann Sorel
 */
public class GridCoverageFinder {
        
    
    /**
     * return a gridcoverage for GeoTiff file. Use a Map containing "url"
     * @param params 
     * @return GridCoverage
     */
    public static GridCoverage getGridCoverage( Map params ){
        boolean found = false;
        GridCoverage cover = null;
        try{
            URL url = (URL) params.get("url");
            File file = new File(url.toURI());
            
            GeoTiffReader reader = new GeoTiffReader( file, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
            GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
            cover = coverage;
            found = true;
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return cover;
    }
    
    
    
}
