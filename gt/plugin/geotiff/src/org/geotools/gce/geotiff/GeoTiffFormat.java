/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's 
 * Fire Science Lab for internal use.  It is therefore ineligible for 
 * copyright under title 17, section 105 of the United States Code.  You 
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the 
 * authors, for free or for compensation.  You may not claim exclusive 
 * ownership of this code because it is already owned by everyone.  Use this 
 * software entirely at your own risk.  No warranty of any kind is given.
 * 
 * A copy of 17-USC-105 should have accompanied this distribution in the file 
 * 17USC105.html.  If not, you may access the law via the US Government's 
 * public websites: 
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package org.geotools.gce.geotiff;

//Geotools dependencies
import org.geotools.cs.CoordinateSystemAuthorityFactory ; 
import org.geotools.data.coverage.grid.AbstractGridFormat;  
import org.geotools.data.coverage.grid.GridCoverageReader ; 
import org.geotools.data.coverage.grid.GridCoverageWriter ; 

//J2SE Dependencies
import java.util.HashMap ; 
import java.io.File ; 

/**
 *
 * @author Bryce Nordgren, USDA Forest Service
 */
public class GeoTiffFormat extends AbstractGridFormat {
    
    /**
     * Holds value of property factory.
     */
    private CoordinateSystemAuthorityFactory factory;
    
    /** Creates a new instance of GeoTiffFormat */
    public GeoTiffFormat() {
        
        // set up the format information map
        mInfo = new HashMap() ; 
        mInfo.put("name", "GeoTIFF") ; 
        mInfo.put("description", "Tagged Image File Format with Geographic information") ; 
        mInfo.put("docURL", "http://FIX ME") ; 
        mInfo.put("version", "1.0") ; 
        mInfo.put("vendor", "Geotools") ; 
        
        factory = org.geotools.cs.CoordinateSystemEPSGFactoryCache.getDefault();
    }
    
    public boolean accepts(Object input) {
        boolean retval = false; 
        
        // If the object is some version of a filename, check the extension
        // for "tif"
        if ((input instanceof File) || (input instanceof String)) {
            String fname = null ; 
            if (input instanceof File) { 
                fname = ((File)input).getName() ; 
            } else {
                fname = (new File((String)input)).getName() ;  
            }
            
            // get the filename extension
            String []fname_parts = fname.split(".") ; 
            
            // accept the input if we have a .tif extension
            retval = fname_parts[fname_parts.length-1].equalsIgnoreCase("tif");
        }
        
        return retval ; 
    }
    
    public GridCoverageReader getReader(Object source) {
        return new GeoTiffReader(this, source) ; 
    }
    
    /**
     * Writing not currently supported.  Always returns null.
     * @return null
     */    
    public GridCoverageWriter getWriter(Object destination) {
        return null ; 
    }
    
    /**
     * Getter for property factory.
     * @return Value of property factory.
     */
    public CoordinateSystemAuthorityFactory getFactory() {
        return this.factory;
    }
    
    /**
     * Setter for property factory.
     * @param factory New value of property factory.
     */
    public void setFactory(CoordinateSystemAuthorityFactory factory) {
        this.factory = factory;
    }
    
}
