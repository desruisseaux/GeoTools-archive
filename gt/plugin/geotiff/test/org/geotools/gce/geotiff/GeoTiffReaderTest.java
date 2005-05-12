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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.geotools.cs.CoordinateSystemAuthorityFactory;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
//import com.sun.media.jai.codec.* ; 

/**
 * <CODE>TiffMetadataOpener</CODE> is a small test program which allows
 * the user to select a Geotiff file, then displays the list of 
 * GeoKeys in text format.
 *
 * @author  Bryce Nordgren / USDA Forest Service
 */
public class GeoTiffReaderTest extends TestCase {
	
	CoordinateSystemAuthorityFactory myfactory;
	
	public GeoTiffReaderTest( String name ){
		super( name );
	}
    
    /* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
        
		// Make an EPSG authority factory which is connected
        // to my postgis database
		/*
        System.out.println("Connecting to EPSG") ;
        Class.forName("org.postgresql.Driver") ; 
        Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost/epsg", "epsg_reader","") ;
        
        CoordinateSystemAuthorityFactory epsgFactory = 
          new CoordinateSystemModifiedEPSGFactory(
             CoordinateSystemFactory.getDefault(),
             conn) ; 
        
        // make the factory cache
        CoordinateSystemAuthorityFactory epsgFactory = 
        	CoordinateSystemEPSGFactoryCache.getDefault() ; 

        Map hints = new HashMap();
        hints.put( CoordinateSystemAuthorityFactory.class, epsgFactory );
        
        // Q: How do we tell the FactoryFinder about this?
         */
	}
	public void testGridCoverate() throws IOException{
        // make a new GeoTiff Format object
        GeoTiffFormat fmt = new GeoTiffFormat() ;
                
        // get the reader object and try constructing the Grid Coverage
        File file = TestData.file( this, "cir.tif" );
        GridCoverageReader reader = fmt.getReader( file );
        assertNull( reader.getCurrentSubname() ); // why not cit?
        assertNull( reader.getMetadataNames() );
        
        GridCoverage gc = reader.read(null) ; 
        assertNotNull( gc );
        assertNotNull( gc.getDimensionNames() );
	}    
	
	public void testImplementation() throws IOException{
        // make a new GeoTiff Format object
        GeoTiffFormat fmt = new GeoTiffFormat() ;
                
        // get the reader object and try constructing the Grid Coverage
        File file = TestData.file( this, "cir.tif" );
        GeoTiffReader reader = (GeoTiffReader) fmt.getReader( file );
        
        assertNull( reader.getCurrentSubname() ); // why not cit?
        assertNull( reader.getMetadataNames() );
        
        GridCoverage gc = reader.read(null) ; 
        assertNotNull( gc );
        assertNotNull( gc.getDimensionNames() );
	}    
}
