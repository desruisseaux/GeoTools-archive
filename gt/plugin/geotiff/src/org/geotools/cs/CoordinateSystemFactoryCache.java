/*
 * CoordinateSystemEPSGFactoryCache.java
 *
 * Created on July 25, 2004, 1:13 PM
 */

package org.geotools.cs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.geotools.units.Unit;
import org.opengis.referencing.FactoryException;

/**
 * This class provides a default implementation of a
 * <CODE>CoordinateSystemAuthorityFactory</CODE> which is capable
 * of constructing complete coordinate systems from a given code.
 * This factory strictly implements a lookup table which is loaded
 * from a text file.
 *
 * <p>
 * The text file must consist of one entry per line.  Each entry must
 * consist of the authority code followed by a &quot;pipe&quot;,
 * followed by a complete Well-Known-Text (WKT) description of the
 * coordinate system to be constructed.
 *
 * <p>
 * The text file may be zipped with PKZIP compression.  If the file
 * is compressed, the first file in the archive, regardless of
 * filename, is taken to be the authority factory database.
 *
 * <p>
 * Subclasses are encouraged to implement <CODE>getDefault()</CODE>
 * style static factory methods to construct and distribute a
 * singleton copy of the Authority database.  This class reads in
 * the entire database when it is instantiated and maintains a Map
 * in memory until <CODE>dispose()</CODE> is called.
 *
 * <p>
 * The only method subclasses must implement in order to be a
 * full-fledged <code>CoordinateSystemAuthorityFactory</code>
 * is &quot;getAuthority()&quot;.
 * @author Bryce Nordgren / USDA Forest Service
 */
public abstract class CoordinateSystemFactoryCache extends CoordinateSystemAuthorityFactory {
    
    private Map csMap = new HashMap();
    
    /** Creates a new instance of CoordinateSystemEPSGFactoryCache */
    protected CoordinateSystemFactoryCache(String fname) throws IOException {
        super(CoordinateSystemFactory.getDefault() ); 
        
        // Open the file, checking first to see that it's a Zip file
        BufferedReader br = null ;
        // is file a zip file? 
        try {
            ZipFile zf = new ZipFile(fname) ; 
            ZipEntry ze = (ZipEntry)(zf.entries().nextElement()) ; 
            InputStream is = zf.getInputStream(ze) ; 
            br = new BufferedReader(new InputStreamReader(is )); 
        } catch (ZipException zex) {
            // If not a zip file, try it as a regular text file
            br = new BufferedReader(new FileReader(fname)) ; 
        }
            
        
        String fileText = br.readLine() ; 
        int line =1 ;
        while (fileText != null) { 
            String []fields = fileText.split("\\|") ; 
            if (fields.length != 2) { 
                throw new IOException("Incorrect # of fields on line: " + line);
            }
            
            // store the Coordinate system to the map.
            csMap.put(fields[0].trim(), fields[1].trim()) ; 
                
            // increment the line number & read the next line
            line ++ ;
            fileText = br.readLine() ; 
        }
        
        br.close() ; 
    }
    
    /**
     * Call this function with the code for the coordinate system.
     * Do not call this function after calling <code>dispose()</code>
     * unless you're fond of <code>NullPointerException</code>s.
     * @param code The code to look up.
     * @return The coordinate system corresponding to the provided code.
     * @throws FactoryException If the code is not located in the database.
     */    
    public CoordinateSystem createCoordinateSystem(String code) throws FactoryException {
        String wkt = (String)(csMap.get(code));
        
        if (wkt == null) { 
            throw new FactoryException("Code "+code+" not found.") ; 
        }
        
        return CoordinateSystemFactory.getDefault().createFromWKT(wkt) ; 
    }
    
    /**
     * Not implemented.
     * @throws UnsupportedOperationException
     * @return nothing
     */    
    public Datum createDatum(String code) throws FactoryException {
        throw new UnsupportedOperationException("The cache does not create Datums") ; 
    }
    
    /**
     * Not implemented.
     * @throws UnsupportedOperationException
     * @return nothing
     */    
    public Ellipsoid createEllipsoid(String code) throws FactoryException {
        throw new UnsupportedOperationException("The cache does not create Ellipsoids") ; 
    }
    
    /**
     * Not implemented.
     * @throws UnsupportedOperationException
     * @return nothing
     */    
    public PrimeMeridian createPrimeMeridian(String code) throws FactoryException {
        throw new UnsupportedOperationException("The cache does not create Prime Meridians") ; 
    }
    
    /**
     * Not implemented.
     * @throws UnsupportedOperationException
     * @return nothing
     */    
    public Unit createUnit(String code) throws FactoryException {
        throw new UnsupportedOperationException("The cache does not create Units") ; 
    }
    
    /**
     * This method should be called when the Authority factory is no longer
     * needed.  It is quite probable that the authority factory database
     * is quite large.  The memory consumed by it should be freed when it is
     * certain that it is no longer needed.
     */    
    public void dispose() {
        csMap = null  ; 
        
        // tell the garbage collector there's a lot of free space.
        System.gc() ; 
    }
    
}
