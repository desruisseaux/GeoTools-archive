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
package org.geotools.arcsde.gce;

import java.awt.Rectangle;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters
 * from an ArcSDE database
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/gce/ArcSDEGCReaderSpatialTest.java $
 * @version $Id: ArcSDEGCReaderSpatialTest.java 28048 2007-11-26 12:53:18Z
 *          groldan $
 */
public class ArcSDEGCReaderSpatialTest extends TestCase {

    private CoordinateReferenceSystem crs;

    private GridGeometry2D highRes;

    private GridGeometry2D medRes;

    private GridGeometry2D lowRes;

    private GridGeometry2D realWordlExampleRes;

    private GridGeometry2D statewideRealWorldExampleRes;

    private Properties conProps;

    /**
     * Creates a new ArcSDEConnectionPoolTest object.
     * 
     */
    public ArcSDEGCReaderSpatialTest(String name) throws Exception {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        // one-time setup
        if (conProps != null)
            return;

        conProps = new Properties();
        String propsFile = "raster-testparams.properties";
        InputStream in = org.geotools.test.TestData.openStream(null, propsFile);
        conProps.load(in);
        in.close();

        crs = CRS.decode("EPSG:26986");
        highRes = new GridGeometry2D(new GeneralGridRange(new Rectangle(1000, 1000)),
                new ReferencedEnvelope(150000.0, 151000.0, 900000.0, 901000.0, crs));
        medRes = new GridGeometry2D(new GeneralGridRange(new Rectangle(500, 500)),
                new ReferencedEnvelope(150000.0, 151000.0, 900000.0, 901000.0, crs));
        lowRes = new GridGeometry2D(new GeneralGridRange(new Rectangle(200, 200)),
                new ReferencedEnvelope(30000.0, 300000.0, 630000.0, 900000.0, crs));
        realWordlExampleRes = new GridGeometry2D(new GeneralGridRange(new Rectangle(256, 128)),
                new ReferencedEnvelope(33000.25, 330000.225, 774000.25, 983400.225, crs));
        // 33,000.25 m 782,500.143 m , 332,999.75 m 953,499.857 m],
        // java.awt.Rectangle[x=0,y=0,width=500,height=285]
        statewideRealWorldExampleRes = new GridGeometry2D(new GeneralGridRange(new Rectangle(500,
                285)), new ReferencedEnvelope(33000.25, 332999.75, 782500.143, 953499.857, crs));

    }

    public void testGetArcSDERasterFormat() throws Exception {

        Format f = new ArcSDERasterFormatFactory().createFormat();
        assertNotNull(f);

    }

    public void testGetArcSDEGC2DReader() throws Exception {

        final String sdeUrl1 = conProps.getProperty("testrasterurl1");
        final String sdeUrl2 = conProps.getProperty("testrasterurl2");
        final String sdeUrl3 = conProps.getProperty("testrasterurl3");
        final String sdeUrl4 = conProps.getProperty("testrasterurl4");

        Format f = new ArcSDERasterFormatFactory().createFormat();

        File hack = new File(sdeUrl1);
        assertTrue(((AbstractGridFormat) f).accepts(hack));
        AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f)
                .getReader(hack);
        assertNotNull(r);
        assertNotNull(r.getOriginalEnvelope());
        assertNotNull(r.getOriginalGridRange());

        assertTrue(((AbstractGridFormat) f).accepts(sdeUrl2));
        r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(sdeUrl2);
        assertNotNull(r);
        assertNotNull(r.getOriginalEnvelope());
        assertNotNull(r.getOriginalGridRange());

        assertTrue(((AbstractGridFormat) f).accepts(sdeUrl3));
        r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(sdeUrl3);
        assertNotNull(r);
        assertNotNull(r.getOriginalEnvelope());
        assertNotNull(r.getOriginalGridRange());

        assertTrue(((AbstractGridFormat) f).accepts(sdeUrl4));
        r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(sdeUrl4);
        assertNotNull(r);
        assertNotNull(r.getOriginalEnvelope());
        assertNotNull(r.getOriginalGridRange());

    }

    public void testRead3BandCoverage() throws Exception {

        String threebandurl = conProps.getProperty("threebandrasterurl");

        GridCoverage2D gc;
        Format f = new ArcSDERasterFormatFactory().createFormat();
        AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f)
                .getReader(threebandurl);

        GeneralParameterValue[] requestParams = new Parameter[1];

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, highRes);
        gc = (GridCoverage2D) r.read(requestParams);
        assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new
        // File("threebandOutput1.png"));
        assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(gc
                .geophysics(true).getRenderedImage(), "threeBandOutput1.png"));

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, medRes);
        gc = (GridCoverage2D) r.read(requestParams);
        assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new
        // File("threeBandOutput2.png"));
        assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(gc
                .geophysics(true).getRenderedImage(), "threeBandOutput2.png"));

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, lowRes);
        gc = (GridCoverage2D) r.read(requestParams);
        assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new
        // File("threeBandOutput3.png"));
        assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(gc
                .geophysics(true).getRenderedImage(), "threeBandOutput3.png"));

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D,
                realWordlExampleRes);
        gc = (GridCoverage2D) r.read(requestParams);
        assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new
        // File("threeBandOutput4.png"));
        assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(gc
                .geophysics(true).getRenderedImage(), "threeBandOutput4.png"));
    }

    public void testRead4BandCoverage() throws Exception {

        String fourbandurl = conProps.getProperty("fourbandrasterurl");

        GridCoverage2D gc;
        Format f = new ArcSDERasterFormatFactory().createFormat();
        AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f)
                .getReader(fourbandurl);

        GeneralParameterValue[] requestParams = new Parameter[1];

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, highRes);
        gc = (GridCoverage2D) r.read(requestParams);
        assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new
        // File("fourbandOutput1.png"));
        assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(gc
                .geophysics(true).getRenderedImage(), "fourbandOutput1.png"));

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, medRes);
        gc = (GridCoverage2D) r.read(requestParams);
        assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new
        // File("fourbandOutput2.png"));
        assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(gc
                .geophysics(true).getRenderedImage(), "fourbandOutput2.png"));

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, lowRes);
        gc = (GridCoverage2D) r.read(requestParams);
        assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new
        // File("fourbandOutput3.png"));
        assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(gc
                .geophysics(true).getRenderedImage(), "fourbandOutput3.png"));

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D,
                realWordlExampleRes);
        gc = (GridCoverage2D) r.read(requestParams);
        assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new
        // File("fourbandOutput4.png"));
        assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(gc
                .geophysics(true).getRenderedImage(), "fourbandOutput4.png"));
    }
}
