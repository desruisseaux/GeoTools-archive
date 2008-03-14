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
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
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

import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeRasterAttr;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters from an ArcSDE database
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEGCReaderSpatialTest extends TestCase {

	private static RasterTestData rasterTestData;
	private static String sderasterurlbase;

	/**
	 * Creates a new ArcSDEConnectionPoolTest object.
	 * 
	 */
	public ArcSDEGCReaderSpatialTest(String name) throws Exception {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ArcSDEGCReaderSpatialTest.class);

		TestSetup wrapper = new TestSetup(suite) {
			@Override
			protected void setUp() throws Exception {
				oneTimeSetUp();
			}

			@Override
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return wrapper;
	}

	private static void oneTimeSetUp() throws Exception {

		rasterTestData = new RasterTestData();
		rasterTestData.setUp();
		rasterTestData.load1bitRaster();
		//rasterTestData.loadRGBRaster();

		ArcSDEConnectionConfig config = rasterTestData.getTestData().getConnectionPool().getConfig();
		// format is sde://user:pass@sdehost:port/[dbname]#rasterTableName
		StringBuffer urlbuilder = new StringBuffer("sde://");
		urlbuilder.append(config.getUserName()).append(":").append(config.getUserPassword());
		urlbuilder.append("@").append(config.getServerName()).append(":").append(config.getPortNumber()).append("/");
		urlbuilder.append(config.getDatabaseName()).append("#");

		sderasterurlbase = urlbuilder.toString();
	}

	private static void oneTimeTearDown() throws Exception {
		rasterTestData.tearDown();
		rasterTestData.getTestData().tearDown(true, true);
	}

	public void testGetArcSDERasterFormat() throws Exception {

		Format f = new ArcSDERasterFormatFactory().createFormat();
		assertNotNull(f);

	}

	public void donttestGetArcSDEGC2DReader() throws Exception {

		// some test urls:
		// sde://user:pass@alexandria.massgis.state.ma.us/gis#GISDATA.IMG_COQ2005
		// sde://user:pass@alexandria.massgis.state.ma.us/#GISDATA.IMG_COQ2005;LZERO_ORIGIN_TILE=0,1438
		// sde://user:pass@alexandria.massgis.state.ma.us/#GISDATA.IMG_COQ2005;FAIL_GRACEFULLY=blahblahblah

		Format f = new ArcSDERasterFormatFactory().createFormat();

		File hack = new File(sderasterurlbase + rasterTestData.get1bitRasterTableName());
		assertTrue(((AbstractGridFormat) f).accepts(hack));
		AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(hack);
		assertNotNull(r);
		assertNotNull(r.getOriginalEnvelope());
		assertNotNull(r.getOriginalGridRange());

		final String sdeUrl2 = sderasterurlbase + rasterTestData.get1bitRasterTableName() + ";LZERO_ORIGIN_TILE=0,100";
		assertTrue(((AbstractGridFormat) f).accepts(sdeUrl2));
		r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(sdeUrl2);
		assertNotNull(r);
		assertNotNull(r.getOriginalEnvelope());
		assertNotNull(r.getOriginalGridRange());

		final String sdeUrl3 = sderasterurlbase + rasterTestData.get1bitRasterTableName() + ";FAIL_GRACEFULLY=blahblah";
		assertTrue(((AbstractGridFormat) f).accepts(sdeUrl3));
		r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(sdeUrl3);
		assertNotNull(r);
		assertNotNull(r.getOriginalEnvelope());
		assertNotNull(r.getOriginalGridRange());

	}

	public void testRead1bitCoverageExact() throws Exception {

		final String oneBitTableName = rasterTestData.get1bitRasterTableName();
		
		GridCoverage2D gc;
		Format f = new ArcSDERasterFormatFactory().createFormat();
		AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(sderasterurlbase + oneBitTableName);

		SeRasterAttr ras = rasterTestData.getRasterAttributes(oneBitTableName, new Rectangle(0, 0, 0, 0), 0, new int[] { 1 });
		int totalheight = ras.getImageHeightByLevel(0);
		int totalwidth = ras.getImageWidthByLevel(0);
		SeExtent ext = ras.getExtentByLevel(0);
		
		GeneralParameterValue[] requestParams = new Parameter[1];
		GridGeometry2D ggr2d = new GridGeometry2D(new GeneralGridRange(new Rectangle(totalwidth, totalheight)), new ReferencedEnvelope(ext.getMinX(), ext.getMaxX(), ext.getMinY(), ext.getMaxY(), r.getCrs()));

		requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, ggr2d);
		gc = (GridCoverage2D) r.read(requestParams);
		assertNotNull(gc);
		//ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));

		final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");
		BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));

		assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(gc.geophysics(true).getRenderedImage(), originalImage));
	}
	
	public void testRead1bitCoverageReproject() throws Exception {

		final String oneBitTableName = rasterTestData.get1bitRasterTableName();
		
		GridCoverage2D gc;
		Format f = new ArcSDERasterFormatFactory().createFormat();
		AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(sderasterurlbase + oneBitTableName);

		SeRasterAttr ras = rasterTestData.getRasterAttributes(oneBitTableName, new Rectangle(0, 0, 0, 0), 0, new int[] { 1 });
		int totalheight = ras.getImageHeightByLevel(0);
		int totalwidth = ras.getImageWidthByLevel(0);
		SeExtent ext = ras.getExtentByLevel(0);
		
		CoordinateReferenceSystem origCrs = r.getCrs();
		ReferencedEnvelope originalFullEnv = new ReferencedEnvelope(ext.getMinX(), ext.getMaxX(), ext.getMinY(), ext.getMaxY(), origCrs);
		ReferencedEnvelope wgs84FullEnv = originalFullEnv.transform(CRS.decode("EPSG:4326"), true);
		
		GeneralParameterValue[] requestParams = new Parameter[1];
		GridGeometry2D ggr2d = new GridGeometry2D(new GeneralGridRange(new Rectangle(totalwidth, totalheight)), wgs84FullEnv);

		requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, ggr2d);
		gc = (GridCoverage2D) r.read(requestParams);
		assertNotNull(gc);
		//ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));

		//err...there's really nothing to compare it to, I guess...
		//final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");
		//BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));

		//assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(gc.geophysics(true).getRenderedImage(), originalImage));
	}

}
