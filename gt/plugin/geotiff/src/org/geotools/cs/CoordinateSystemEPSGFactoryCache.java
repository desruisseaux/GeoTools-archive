/*
 * CoordinateSystemEPSGFactoryCache.java
 *
 * Created on July 25, 2004, 2:18 PM
 */

package org.geotools.cs;
import java.io.IOException ;
import java.net.URL ; 

/**
 * This class implements a file-system-based EPSG Coordinate System
 * factory.  The functionality is limited to creating a complete
 * <code>CoordinateSystem</code> object from the code used to
 * refer to it.  Any other factory method will throw an
 * <code>UnsupportedOperationException</code>.
 *
 * <p>
 * Calling <code>getDefault()</code> will return the default instance
 * which contains the entire EPSG database, as represented by the
 * PostGIS <code>spatial_ref_sys</code> table.  The EPSG database
 * is not loaded into memory until the first call to
 * <code>getDefault()</code>.
 *
 * <p>A constructor is provided so that users may create their own
 * subset of the EPSG database.  This is provided to conserve
 * memory in the event that users determine that only certain codes
 * are required.
 * @author Bryce Nordgren / USDA Forest Service
 */
public class CoordinateSystemEPSGFactoryCache extends CoordinateSystemFactoryCache {
    
    private static CoordinateSystemEPSGFactoryCache defaultInstance = null;
    
    /**
     * Creates a new instance of CoordinateSystemEPSGFactoryCache.  The
     * named file contains the database of mappings between codes and
     * WKT descriptions of the coordinate systems.
     * @see org.geotools.cs.CoordinateSystemFactoryCache
     */
    public CoordinateSystemEPSGFactoryCache(String fname) throws IOException {
        super(fname);  
    }
    
    /**
     * Gives information as to which authority constructed the
     * coordinate system database.
     * @return "EPSG"
     */    
    public String getAuthority() {
        return "EPSG" ; 
    }
    
    /**
     * This method retrieves the default instance of the EPSG file
     * cache database.  Retrieving this instance loads the entire
     * database into memory.
     *
     * <p>
     * The default instance is a Singleton, so multiple calls to
     * <code>getDefault()</code> will return only one reference.
     */    
    public static CoordinateSystemEPSGFactoryCache getDefault() {
        try {
            if (defaultInstance == null){
                URL resource = CoordinateSystemEPSGFactoryCache.class.getResource("data/epsg.zip") ; 
                defaultInstance = new CoordinateSystemEPSGFactoryCache(resource.getPath());
            }
        } catch (IOException ioe) { 
            throw new NullPointerException("ERROR Loading EPSG database!") ; 
        }
        
        return defaultInstance ; 
    }
    
    /**
     * Because we are issuing references to a singleton, we don't want
     * calls to <code>dispose()</code> to sneak through and nuke a reference
     * someone else might be using.  Only <code>dispose()</code> if the user
     * made this object themselves.
     */    
    public void dispose() {
        if (this != defaultInstance) {
            super.dispose() ; 
        }
    }
    
}
