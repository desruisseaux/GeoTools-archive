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

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.geotools.data.coverage.grid.file.FileSystemGridCoverageExchange;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Projection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class GeoTiffTest extends TestCase {
    public GeoTiffTest() {
        super("Basic Geotiff tests");
    }

    public static void main(String[] args) {
        TestRunner.run(GeoTiffTest.class);
    }

    /**
     * Tests that there is a WORKING epsg CRS authority factory.
     *
     * @throws FactoryException DOCUMENT ME!
     */
    public void testEpsgFactory() throws FactoryException {
        CRSAuthorityFactory crs = FactoryFinder.getCRSAuthorityFactory("EPSG",
                null);

        // try to instantiate GCS NAD 1983
        GeographicCRS gcs = crs.createGeographicCRS("EPSG:4269");
        assertNotNull(gcs);
    }

    public boolean isAuthorityFactoryAvailable() {
        boolean available = false;

        try {
            FactoryFinder.getCSAuthorityFactory("EPSG", null);
            available = true;
        } catch (FactoryRegistryException fe) {
            // state captured by "available = false"
        }

        return available;
    }

    /**
     * Prints out the list of available math transforms.  Succeeds if  a
     * mathtransformfactory can be found and used.
     */
    public void testMathTransformFactory() {
        // This checks if FactoryFinder can find a factory
        MathTransformFactory mtf = FactoryFinder.getMathTransformFactory(null);
        assertNotNull("No Math Transform Factory found!", mtf);

        //////////////////////////////////////////////////////////////
        // Geotools specific code to print out list of transforms.
        String[] dummy = new String[1];
        dummy[0] = "-projections";

        DefaultMathTransformFactory.main(dummy);
    }

    /**
     * Checks each of the items in the "mapCoordTrans" map to ensure that  the
     * key is contained in the MathTransformFactory's "available methods".
     */
    public void testCoordinateTransformationMap() {
        MathTransformFactory mtf = FactoryFinder.getMathTransformFactory(null);
        Map map = GeoTiffCoordinateSystemAdapter.getCoordTransMap();

        Set validXforms = mtf.getAvailableMethods(Projection.class);
        assertNotNull("No methods returned!", validXforms);
        assertFalse("Empty set returned!", validXforms.isEmpty());

        // find out which ones aren't legal, as well as what's allowed
        StringBuffer msg = new StringBuffer();
        StringBuffer goodXformNames = new StringBuffer();

        // assemble a list of what's allowed.
        Iterator goodXforms = validXforms.iterator();
        Set goodNameSet = new HashSet();

        while (goodXforms.hasNext()) {
            IdentifiedObject io = (IdentifiedObject) (goodXforms.next());
            String curName = (String) (io.getName().getCode());

            // assemble a list of good transform names
            goodNameSet.add(curName);
            goodXformNames.append(curName);

            if (goodXforms.hasNext()) {
                goodXformNames.append(", ");
            }
        }

        goodXformNames.append(".\n");

        // assemble a list of what I have listed.
        Iterator myXforms = map.values().iterator();

        while (myXforms.hasNext()) {
            String curName = (String) (myXforms.next());

            // is the current name valid?
            if (!goodNameSet.contains(curName)) {
                msg.append(curName + " not a valid transform!\n");
            }
        }

        // Check that all of MY names are contained in the reference
        // Set returned by the  MathTransformFactory
        boolean allGood = goodNameSet.containsAll(map.values());

        assertTrue(goodXformNames.append(msg).toString(), allGood);
    }

    /**
     * Checks that default parameters are available for each of the
     * MathTransforms I have registered.
     */
    public void testDefaultParametersAvailable() {
        Iterator it = GeoTiffCoordinateSystemAdapter.getCoordTransMap().values()
                                                    .iterator();
        MathTransformFactory mtf = FactoryFinder.getMathTransformFactory(null);

        while (it.hasNext()) {
            String myName = null;

            try {
                myName = (String) (it.next());

                ParameterValueGroup pvg = mtf.getDefaultParameters(myName);
                assertNotNull("No params for: " + myName, pvg);
            } catch (FactoryException nsi) {
                fail("Unrecognized parameter: " + myName);
            }
        }
    }

    /**
     * Tests that the reader will refuse to read jpeg (a non-tiff)
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testRefuseJpeg() throws IOException {
        GeoTiffFormat fmt = new GeoTiffFormat();
        File testFile = TestData.file(GeoTiffTest.class, "fire.jpg");
        assertNotNull("Framework error: no test data!", testFile);

        assertFalse("Accepting JPEGS!", fmt.accepts(testFile));
    }

    /**
     * Tests that the reader will refuse to read tiff files without a  geokey
     * directory (non-GeoTiffs)
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testRefuseRegularTiff() throws IOException {
        GeoTiffFormat fmt = new GeoTiffFormat();
        File testFile = TestData.file(GeoTiffTest.class, "fire.tif");
        assertNotNull("Framework error: no test data!", testFile);

        assertFalse("Accepting non-GeoTIFFs!", fmt.accepts(testFile));
    }

    /**
     * Tests that the reader will refuse to read tiff files without a  geokey
     * directory (non-GeoTiffs)
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testAcceptGeoTiff() throws IOException {
        GeoTiffFormat fmt = new GeoTiffFormat();
        File testFile = TestData.file(GeoTiffTest.class, "cir.tif");
        assertNotNull("Framework error: no test data!", testFile);

        assertTrue("Not accepting GeoTIFFs!", fmt.accepts(testFile));
    }

    /**
     * Tests that the GeoTiffReader can be instantiated directly. NOTE: Users
     * should never have to do this, but if it can't be done, then nothing
     * else will work.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testReaderCreation() throws IOException {
        GeoTiffFormat fmt = new GeoTiffFormat();
        File testFile = TestData.file(GeoTiffTest.class, "cir.tif");
        assertNotNull("Framework error: no test data!", testFile);

        GeoTiffReader rdr = (GeoTiffReader) (fmt.getReader(testFile));
        assertNotNull(rdr);
    }

    /**
     * Reads a given file with the GeoTiffReader.  Returns the  resultant
     * image.  If there's an error reading the file, or  constructing the CRS
     * from the tags, it should throw a GeoTiffException.
     *
     * @param file2Read DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public GridCoverage readFile(File file2Read) throws IOException {
        GeoTiffFormat fmt = new GeoTiffFormat();

        GeoTiffReader rdr = (GeoTiffReader) (fmt.getReader(file2Read));
        assertNotNull(rdr);

        return rdr.read(null);
    }

    /**
     * Attempts to read a very simple test case: an ESRI-written,  GCS NAD1983.
     * Errors if it can't construct the CRS.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testEsriGcsNad1983() throws IOException {
        File testFile = TestData.file(GeoTiffTest.class, "cir.tif");
        GridCoverage gc = readFile(testFile);
    }

    /**
     * Tests that the GeoTiffReader can be instantiated with the
     * FileSystemGridCoverageExchange().  This is really checking to  see if
     * I've registered everything correctly and everything is right with the
     * world.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testFileSystemGCE() throws IOException {
        File testFile = TestData.file(GeoTiffTest.class, "cir.tif");
        assertNotNull("Framework error: no test data!", testFile);

        FileSystemGridCoverageExchange gce = new FileSystemGridCoverageExchange();
        gce.add(testFile);

        // get an iterator over catalog entries
        Iterator it = gce.iterator();
        assertNotNull("Can't get iterator from FileSystemGCE.", it);

        // Get the file object
        Object myTestFile = it.next();
        assertNotNull("No files in the catalog entry!", myTestFile);

        // FINALLY, make the reader
        GridCoverageReader rdr = gce.getReader(myTestFile);

        // This ensures that we got a GEOTIFF GridCoverageReader, as opposed to
        // something else.
        GeoTiffReader geotiff = (GeoTiffReader) rdr;
    }

    /**
     * Tests that the GeoTiffReader can read one of the ESRI standard  Albers
     * Equal Area projections.  This is not totally specified by the  EPSG and
     * so exercises some of the &quot;custom&quot; code.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testReadESRIAlbers() throws IOException {
        // skip if authority factories are not available
        if (!isAuthorityFactoryAvailable()) {
            return;
        }

        File testFile = TestData.file(GeoTiffTest.class, "non-arc-meghan.tif");
        GridCoverage gc = readFile(testFile);
    }
}
