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
package org.geotools.gce.geotiff ; 

import junit.framework.TestCase ; 
import junit.textui.TestRunner ; 

import java.io.File ; 
import java.io.IOException ; 
import java.util.Iterator ; 
import java.net.URL ; 

import org.geotools.data.coverage.grid.file.FileSystemGridCoverageExchange ; 
import org.geotools.resources.TestData ; 
import org.geotools.referencing.FactoryFinder ;

import org.opengis.referencing.crs.CRSAuthorityFactory ; 
import org.opengis.referencing.crs.GeographicCRS ; 
import org.opengis.coverage.grid.GridCoverageReader ; 
import org.opengis.coverage.grid.GridCoverage ; 
import org.opengis.referencing.FactoryException ;

public class GeoTiffTest extends TestCase {

  public GeoTiffTest() {
    super("Basic Geotiff tests") ; 
  }

  public static void main(String []args) {
    TestRunner.run(GeoTiffTest.class) ; 
  }

  /**
   * Tests that there is a WORKING epsg CRS authority factory.
   */
  public void testEpsgFactory() throws FactoryException { 
    CRSAuthorityFactory crs = FactoryFinder.getCRSAuthorityFactory("EPSG",null);

    // try to instantiate GCS NAD 1983
    GeographicCRS gcs = crs.createGeographicCRS("EPSG:4269") ; 
    assertNotNull(gcs) ; 
  }

  /**
   * Tests that the GeoTiffReader can be instantiated directly.
   * NOTE: Users should never have to do this, but if it can't
   * be done, then nothing else will work.
   */
  public void testReaderCreation() throws IOException { 
    GeoTiffFormat fmt = new GeoTiffFormat() ; 
    File testFile = TestData.file(GeoTiffTest.class, "cir.tif") ; 
    assertNotNull("Framework error: no test data!", testFile) ; 

    GeoTiffReader rdr = (GeoTiffReader)(fmt.getReader(testFile)) ; 
    assertNotNull(rdr) ; 
  }

  /**
   * Reads a given file with the GeoTiffReader.  Returns the 
   * resultant image.  If there's an error reading the file, or 
   * constructing the CRS from the tags, it should throw a
   * GeoTiffException.
   */
  public GridCoverage readFile(File file2Read) throws IOException { 
    GeoTiffFormat fmt = new GeoTiffFormat() ; 

    GeoTiffReader rdr = (GeoTiffReader)(fmt.getReader(file2Read)) ; 
    assertNotNull(rdr) ; 

    return rdr.read(null) ; 
  }

  /**
   * Attempts to read a very simple test case: an ESRI-written, 
   * GCS NAD1983.  Errors if it can't construct the CRS.
   */
  public void testEsriGcsNad1983() throws IOException { 
    File testFile = TestData.file(GeoTiffTest.class, "cir.tif") ; 
    GridCoverage gc = readFile(testFile) ; 
  }

  /**
   * Tests that the GeoTiffReader can be instantiated with the 
   * FileSystemGridCoverageExchange().  This is really checking to 
   * see if I've registered everything correctly and everything is right
   * with the world.
   */
  public void testFileSystemGCE() throws IOException { 
    File testFile = TestData.file(GeoTiffTest.class, "cir.tif") ; 
    assertNotNull("Framework error: no test data!", testFile) ; 

    FileSystemGridCoverageExchange gce = new FileSystemGridCoverageExchange();
    gce.add(testFile) ; 
    
    // get an iterator over catalog entries
    Iterator it = gce.iterator() ; 
    assertNotNull("Can't get iterator from FileSystemGCE.", it) ; 

    // Get the file object
    Object myTestFile = it.next() ; 
    assertNotNull("No files in the catalog entry!", myTestFile) ; 

    // FINALLY, make the reader
    GridCoverageReader rdr = gce.getReader(myTestFile) ; 

    // This ensures that we got a GEOTIFF GridCoverageReader, as opposed to
    // something else.
    GeoTiffReader geotiff = (GeoTiffReader)rdr ; 
  }

}
