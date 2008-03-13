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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.arcsde.data.ArcSDEDataStoreTest;
import org.geotools.arcsde.gce.band.ArcSDERasterBandCopier;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters from an ArcSDE database.
 * 
 * This class in particular tests the class which reads data from the underlying raster tile and copies it to a java.awt.Raster for display.
 * 
 * @author Saul Farber
 * @source $URL$
 * @version $Id$
 */
public class BandCopierTest extends TestCase {

	private Logger LOGGER = Logging.getLogger(this.getClass());
	private static RasterTestData rasterTestData;

	public BandCopierTest(String name) {
		super(name);
	}
	
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(BandCopierTest.class);

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
		if (rasterTestData == null) {
			rasterTestData = new RasterTestData();
			rasterTestData.setUp();
			rasterTestData.load1bitRaster();
			rasterTestData.loadRGBRaster();
		}
	}

	private static void oneTimeTearDown() throws Exception {
		if (rasterTestData != null) {
			rasterTestData.tearDown();
		}
	}
	
	public void testLiveOneBitAlignedRasterTile() throws Exception {
		final String tableName = rasterTestData.get1bitRasterTableName();
		
		ArcSDEPooledConnection scon = null;
		try {
			ArcSDEConnectionPool pool = rasterTestData.getTestData().getConnectionPool();

			scon = pool.getConnection();
			SeQuery q = new SeQuery(scon, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
			q.prepareQuery();
			q.execute();
			SeRow r = q.fetch();
			SeRasterAttr rAttr = r.getRaster(0);

			int[] bands = new int[] { 1 };
			SeRasterConstraint rConstraint = new SeRasterConstraint();
			rConstraint.setBands(bands);
			rConstraint.setLevel(0);
			rConstraint.setEnvelope(0, 0, 0, 0);
			rConstraint.setInterleave(SeRaster.SE_RASTER_INTERLEAVE_BSQ);

			q.queryRasterTile(rConstraint);

			BufferedImage fromSdeImage = new BufferedImage(128,128, BufferedImage.TYPE_BYTE_BINARY);
			ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(rAttr.getPixelType(), rAttr.getTileWidth(), rAttr.getTileHeight());

			SeRasterTile rTile = r.getRasterTile();
			for (int i = 0; i < bands.length; i++) {
				bandCopier.copyPixelData(rTile, fromSdeImage.getRaster(), 0, 0, i);
				rTile = r.getRasterTile();
			}

			ImageIO.write(fromSdeImage, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final File originalRasterFile = org.geotools.test.TestData.file(null, rasterTestData.getRasterTestDataProperty("sampledata.onebitraster"));
			BufferedImage originalImage = ImageIO.read(originalRasterFile);

			// Well, now we have an image tile. Does it have what we expect on it?
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(fromSdeImage, originalImage.getSubimage(0, 0, 128, 128)));
			
		} catch (SeException se) {
			LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
		} finally {
			if (scon != null)
				scon.close();
		}
	}
	
	public void testLiveOneBitUnalignedRasterTile() throws Exception {
		final String tableName = rasterTestData.get1bitRasterTableName();
		
		ArcSDEPooledConnection scon = null;
		try {
			ArcSDEConnectionPool pool = rasterTestData.getTestData().getConnectionPool();

			scon = pool.getConnection();
			SeQuery q = new SeQuery(scon, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
			q.prepareQuery();
			q.execute();
			SeRow r = q.fetch();
			SeRasterAttr rAttr = r.getRaster(0);

			int[] bands = new int[] { 1 };
			SeRasterConstraint rConstraint = new SeRasterConstraint();
			rConstraint.setBands(bands);
			rConstraint.setLevel(0);
			rConstraint.setEnvelope(0, 0, 0, 0);
			rConstraint.setInterleave(SeRaster.SE_RASTER_INTERLEAVE_BSQ);

			q.queryRasterTile(rConstraint);
			
			final int targetImgWidth = 67, targetImgHeight = 67;
			final int imgxstart=38, imgystart=31;

			BufferedImage fromSdeImage = new BufferedImage(targetImgWidth,targetImgHeight, BufferedImage.TYPE_BYTE_BINARY);
			ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(rAttr.getPixelType(), rAttr.getTileWidth(), rAttr.getTileHeight());

			SeRasterTile rTile = r.getRasterTile();
			for (int i = 0; i < bands.length; i++) {
				bandCopier.copyPixelData(rTile, fromSdeImage.getRaster(), imgxstart, imgystart, i);
				rTile = r.getRasterTile();
			}

			final File originalRasterFile = org.geotools.test.TestData.file(null, rasterTestData.getRasterTestDataProperty("sampledata.onebitraster"));
			BufferedImage originalImage = ImageIO.read(originalRasterFile);
			BufferedImage subImage = originalImage.getSubimage(imgxstart, imgystart, targetImgWidth, targetImgHeight);

			// Well, now we have an image tile. Does it have what we expect on it?
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(fromSdeImage, subImage));
			
		} catch (SeException se) {
			LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
		} finally {
			if (scon != null)
				scon.close();
		}
	}

	public void testLiveRGBRasterTile() throws Exception {
		final String tableName = rasterTestData.getRGBRasterTableName();

		ArcSDEPooledConnection scon = null;
		try {
			ArcSDEConnectionPool pool = rasterTestData.getTestData().getConnectionPool();

			scon = pool.getConnection();
			SeQuery q = new SeQuery(scon, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
			q.prepareQuery();
			q.execute();
			SeRow r = q.fetch();
			SeRasterAttr rAttr = r.getRaster(0);

			int[] bands = new int[] { 1, 2, 3 };
			SeRasterConstraint rConstraint = new SeRasterConstraint();
			rConstraint.setBands(bands);
			rConstraint.setLevel(0);
			rConstraint.setEnvelope(0, 0, 0, 0);
			rConstraint.setInterleave(SeRaster.SE_RASTER_INTERLEAVE_BSQ);

			q.queryRasterTile(rConstraint);

			BufferedImage fromSdeImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
			ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(rAttr.getPixelType(), rAttr.getTileWidth(), rAttr.getTileHeight());

			SeRasterTile rTile = r.getRasterTile();
			for (int i = 0; i < bands.length; i++) {
				bandCopier.copyPixelData(rTile, fromSdeImage.getRaster(), 0, 0, i);
				rTile = r.getRasterTile();
			}

			final File originalRasterFile = org.geotools.test.TestData.file(null, rasterTestData.getRasterTestDataProperty("sampledata.rgbraster"));
			BufferedImage originalImage = ImageIO.read(originalRasterFile);

			// Well, now we have an image tile. Does it have what we expect on it?
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(fromSdeImage, originalImage.getSubimage(0, 0, 128, 128)));

		} catch (SeException se) {
			LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
		} finally {
			if (scon != null)
				scon.close();
		}
	}
}
