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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.arcsde.gce.imageio.ArcSDERasterImageReadParam;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReader;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReaderSpi;
import org.geotools.arcsde.pool.Session;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters from an ArcSDE database
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEImageIOReaderFunctionalTest extends TestCase {

	private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.arcsde.gce");

	static RasterTestData rasterTestData;
	static HashMap<String, Object> fourBandReaderProps, threeBandReaderProps, oneBitReaderProps;
	static SeRasterAttr rattrThreeBand, rattrFourBand, rattrOneBit;

	/**
	 * Creates a new ArcSDEConnectionPoolTest object.
	 * 
	 * Lots of one-time setup operations in here. Rather than re-do everything for each test, it's just done once in the class constructor.
	 * 
	 * Not sure how this jives with JUnits testing framework though...
	 * 
	 */
	public ArcSDEImageIOReaderFunctionalTest(String name) throws Exception {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ArcSDEImageIOReaderFunctionalTest.class);

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
		rasterTestData.loadRGBRaster();

		Session scon = null;
		SeQuery q = null;
		ArcSDEPyramid pyramid;
		SeRow r;
		String tableName;
		try {

			// Set up a pyramid and readerprops for the sample three-band imagery
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();
			tableName = rasterTestData.getRGBRasterTableName();
			q = scon.createSeQuery(new String[] { "RASTER" }, new SeSqlConstruct(tableName));
			q.prepareQuery();
			q.execute();
			r = q.fetch();
			rattrThreeBand = r.getRaster(0);
			q.close();

			SeRasterColumn rcol = scon.createSeRasterColumn(rattrThreeBand.getRasterColumnId());

			CoordinateReferenceSystem crs = CRS.parseWKT(rcol.getCoordRef().getCoordSysDescription());
			pyramid = new ArcSDEPyramid(rattrThreeBand, crs);

			threeBandReaderProps = new HashMap<String, Object>();
			threeBandReaderProps.put(ArcSDERasterReaderSpi.PYRAMID, pyramid);
			threeBandReaderProps.put(ArcSDERasterReaderSpi.RASTER_TABLE, tableName);
			threeBandReaderProps.put(ArcSDERasterReaderSpi.RASTER_COLUMN, "RASTER");
		} catch (SeException se) {
			LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
			throw se;
		} finally {
			if (q != null)
				q.close();
			if (scon != null) {
				scon.close();
			}
		}
		
		try {

			// Set up a pyramid and readerprops for the sample 1-bit imagery
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();
			tableName = rasterTestData.get1bitRasterTableName();
			q = scon.createSeQuery(new String[] { "RASTER" }, new SeSqlConstruct(tableName));
			q.prepareQuery();
			q.execute();
			r = q.fetch();
			rattrOneBit = r.getRaster(0);
			q.close();

			SeRasterColumn rcol = scon.createSeRasterColumn(rattrOneBit.getRasterColumnId());

			CoordinateReferenceSystem crs = CRS.parseWKT(rcol.getCoordRef().getCoordSysDescription());
			pyramid = new ArcSDEPyramid(rattrOneBit, crs);

			oneBitReaderProps = new HashMap<String, Object>();
			oneBitReaderProps.put(ArcSDERasterReaderSpi.PYRAMID, pyramid);
			oneBitReaderProps.put(ArcSDERasterReaderSpi.RASTER_TABLE, tableName);
			oneBitReaderProps.put(ArcSDERasterReaderSpi.RASTER_COLUMN, "RASTER");
		} catch (SeException se) {
			LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
			throw se;
		} finally {
			if (q != null)
				q.close();
			if (scon != null) {
				scon.close();
			}
		}
	}

	private static void oneTimeTearDown() throws Exception {
		rasterTestData.tearDown();
		rasterTestData.getTestData().tearDown(true, true);
	}

	/**
	 * Tests reading the first three bands of a 4-band image (1 = RED, 2 = GREEN, 3 = BLUE, 4 = NEAR_INFRARED) into a TYPE_INT_RGB image.
	 * 
	 * Bands are mapped as follows: rasterband 1 => image band 0 rasterband 2 => image band 1 rasterband 3 => image band 2
	 * 
	 */
	public void testReadOutsideRGBImageBounds() throws Exception {

		ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(threeBandReaderProps);

		Session scon = null;
		try {
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();

			SeRasterBand[] bands = rattrThreeBand.getBands();
			HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

			bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
			bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
			bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

			BufferedImage image;
			//int[] opaque;

			final Point dataOffset = new Point(950, 950);
			final int w = 300, h = 300;

			ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
			rParam.setSourceBands(new int[] { 1, 2, 3 });
			rParam.setConnection(scon);
			rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
			image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					image.setRGB(x, y, 0xffffffff);
				}
			}
			rParam.setDestination(image);
			rParam.setBandMapper(bandMapper);

			reader.read(0, rParam);

			// ImageIO.write(image, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.rgbraster");
			BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));

			final int sourcemaxw = Math.min(rattrThreeBand.getImageWidthByLevel(0) - dataOffset.x, w);
			final int sourcemaxh = Math.min(rattrThreeBand.getImageHeightByLevel(0) - dataOffset.y, h);
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image.getSubimage(0, 0, sourcemaxw, sourcemaxh), originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh)));

		} catch (Exception e) {
			throw e;
		} finally {
			if (scon != null && !scon.isClosed())
				scon.close();
		}
	}

	public void testReadOffsetRGBImage() throws Exception {

		ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(threeBandReaderProps);

		Session scon = null;
		try {
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();

			SeRasterBand[] bands = rattrThreeBand.getBands();
			HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

			bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
			bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
			bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

			BufferedImage image;
			//int[] opaque;

			final Point dataOffset = new Point(0, 0);
			final Point imageOffset = new Point(100, 100);
			final int w = 1200, h = 1200;

			image = new BufferedImage(w + imageOffset.x, h + imageOffset.y, BufferedImage.TYPE_INT_RGB);

			ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
			rParam.setSourceBands(new int[] { 1, 2, 3 });
			rParam.setConnection(scon);
			rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
			rParam.setDestination(image);
			rParam.setDestinationOffset(imageOffset);
			rParam.setBandMapper(bandMapper);

			reader.read(0, rParam);

			//ImageIO.write(image, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.rgbraster");
			BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));

			final int sourcemaxw = Math.min(rattrThreeBand.getImageWidthByLevel(0) - dataOffset.x, w);
			final int sourcemaxh = Math.min(rattrThreeBand.getImageHeightByLevel(0) - dataOffset.y, h);
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh), originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh)));

		} catch (Exception e) {
			throw e;
		} finally {
			if (scon != null && !scon.isClosed())
				scon.close();
		}
	}
	
	public void testRead1bitImageTileAligned() throws Exception {
		ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(oneBitReaderProps);

		Session scon = null;
		try {
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();

			SeRasterBand[] bands = rattrOneBit.getBands();
			HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

			bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

			BufferedImage image;

			final Point dataOffset = new Point(0, 0);
			final Point imageOffset = new Point(0, 0);
			final int w = 256, h = 256;

			image = new BufferedImage(w + imageOffset.x, h + imageOffset.y, BufferedImage.TYPE_BYTE_BINARY);

			ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
			rParam.setSourceBands(new int[] { 1 });
			rParam.setConnection(scon);
			rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
			rParam.setDestination(image);
			rParam.setDestinationOffset(imageOffset);
			rParam.setBandMapper(bandMapper);

			reader.read(0, rParam);

			//ImageIO.write(image, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");

			final int sourcemaxw = Math.min(rattrOneBit.getImageWidthByLevel(0) - dataOffset.x, w);
			final int sourcemaxh = Math.min(rattrOneBit.getImageHeightByLevel(0) - dataOffset.y, h);
			BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));
			originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh);
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
					image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
					originalImage));

		} catch (Exception e) {
			throw e;
		} finally {
			if (scon != null && !scon.isClosed())
				scon.close();
		}
	}
	
	public void testRead1bitImageByteAligned() throws Exception {
		ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(oneBitReaderProps);

		Session scon = null;
		try {
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();

			SeRasterBand[] bands = rattrOneBit.getBands();
			HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

			bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

			BufferedImage image;

			final Point dataOffset = new Point(8, 8);
			final Point imageOffset = new Point(0, 0);
			final int w = 256, h = 256;

			image = new BufferedImage(w + imageOffset.x, h + imageOffset.y, BufferedImage.TYPE_BYTE_BINARY);

			ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
			rParam.setSourceBands(new int[] { 1 });
			rParam.setConnection(scon);
			rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
			rParam.setDestination(image);
			rParam.setDestinationOffset(imageOffset);
			rParam.setBandMapper(bandMapper);

			reader.read(0, rParam);

			//ImageIO.write(image, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");

			final int sourcemaxw = Math.min(rattrOneBit.getImageWidthByLevel(0) - dataOffset.x, w);
			final int sourcemaxh = Math.min(rattrOneBit.getImageHeightByLevel(0) - dataOffset.y, h);
			BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));
			originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh);
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
					image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
					originalImage));

		} catch (Exception e) {
			throw e;
		} finally {
			if (scon != null && !scon.isClosed())
				scon.close();
		}
	}
	
	public void testRead1bitImageDataOffset1() throws Exception {
		ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(oneBitReaderProps);

		Session scon = null;
		try {
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();

			SeRasterBand[] bands = rattrOneBit.getBands();
			HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

			bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

			BufferedImage image;

			final Point dataOffset = new Point(3, 3);
			final Point imageOffset = new Point(0, 0);
			final int w = 128, h = 128;

			image = new BufferedImage(w + imageOffset.x, h + imageOffset.y, BufferedImage.TYPE_BYTE_BINARY);

			ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
			rParam.setSourceBands(new int[] { 1 });
			rParam.setConnection(scon);
			rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
			rParam.setDestination(image);
			rParam.setDestinationOffset(imageOffset);
			rParam.setBandMapper(bandMapper);

			reader.read(0, rParam);

			//ImageIO.write(image, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");

			final int sourcemaxw = Math.min(rattrOneBit.getImageWidthByLevel(0) - dataOffset.x, w);
			final int sourcemaxh = Math.min(rattrOneBit.getImageHeightByLevel(0) - dataOffset.y, h);
			BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));
			originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh);
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
					image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
					originalImage));

		} catch (Exception e) {
			throw e;
		} finally {
			if (scon != null && !scon.isClosed())
				scon.close();
		}
	}
	
	public void testRead1bitImageDataOffset2() throws Exception {
		ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(oneBitReaderProps);

		Session scon = null;
		try {
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();

			SeRasterBand[] bands = rattrOneBit.getBands();
			HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

			bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

			BufferedImage image;

			final Point dataOffset = new Point(15, 15);
			final Point imageOffset = new Point(0, 0);
			final int w = 176, h = 176;

			image = new BufferedImage(w + imageOffset.x, h + imageOffset.y, BufferedImage.TYPE_BYTE_BINARY);

			ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
			rParam.setSourceBands(new int[] { 1 });
			rParam.setConnection(scon);
			rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
			rParam.setDestination(image);
			rParam.setDestinationOffset(imageOffset);
			rParam.setBandMapper(bandMapper);

			reader.read(0, rParam);

			//ImageIO.write(image, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");

			final int sourcemaxw = Math.min(rattrOneBit.getImageWidthByLevel(0) - dataOffset.x, w);
			final int sourcemaxh = Math.min(rattrOneBit.getImageHeightByLevel(0) - dataOffset.y, h);
			BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));
			//ImageIO.write(originalImage.getSubimage(0, 0, 200, 200), "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + "-original.png"));
			originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh);
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
					image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
					originalImage));

		} catch (Exception e) {
			throw e;
		} finally {
			if (scon != null && !scon.isClosed())
				scon.close();
		}
	}
	
	public void testRead1bitImageTargetImageOffset1() throws Exception {
		ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(oneBitReaderProps);

		Session scon = null;
		try {
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();

			SeRasterBand[] bands = rattrOneBit.getBands();
			HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

			bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

			BufferedImage image;

			final Point dataOffset = new Point(0, 0);
			final Point imageOffset = new Point(5, 5);
			final int w = 176, h = 176;

			image = new BufferedImage(w + imageOffset.x, h + imageOffset.y, BufferedImage.TYPE_BYTE_BINARY);

			ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
			rParam.setSourceBands(new int[] { 1 });
			rParam.setConnection(scon);
			rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
			rParam.setDestination(image);
			rParam.setDestinationOffset(imageOffset);
			rParam.setBandMapper(bandMapper);

			reader.read(0, rParam);

			//ImageIO.write(image, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");

			final int sourcemaxw = Math.min(rattrOneBit.getImageWidthByLevel(0) - dataOffset.x, w);
			final int sourcemaxh = Math.min(rattrOneBit.getImageHeightByLevel(0) - dataOffset.y, h);
			BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));
			//ImageIO.write(originalImage.getSubimage(0, 0, 200, 200), "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + "-original.png"));
			originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh);
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
					image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
					originalImage));

		} catch (Exception e) {
			throw e;
		} finally {
			if (scon != null && !scon.isClosed())
				scon.close();
		}
	}
	
	public void testRead1bitImageTargetImageBeyondBoundaries1() throws Exception {
		ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(oneBitReaderProps);

		Session scon = null;
		try {
			scon = rasterTestData.getTestData().getConnectionPool().getConnection();

			SeRasterBand[] bands = rattrOneBit.getBands();
			HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

			bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

			BufferedImage image;

			final Point dataOffset = new Point(400, 400);
			final Point imageOffset = new Point(30, 30);
			final int w = 176, h = 176;

			image = new BufferedImage(w + imageOffset.x, h + imageOffset.y, BufferedImage.TYPE_BYTE_BINARY);

			ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
			rParam.setSourceBands(new int[] { 1 });
			rParam.setConnection(scon);
			rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
			rParam.setDestination(image);
			rParam.setDestinationOffset(imageOffset);
			rParam.setBandMapper(bandMapper);

			reader.read(0, rParam);

			//ImageIO.write(image, "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
			final String rasFileName = rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");

			final int sourcemaxw = Math.min(rattrOneBit.getImageWidthByLevel(0) - dataOffset.x, w);
			final int sourcemaxh = Math.min(rattrOneBit.getImageHeightByLevel(0) - dataOffset.y, h);
			BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null, rasFileName));
			//ImageIO.write(originalImage.getSubimage(0, 0, 200, 200), "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + "-original.png"));
			originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh);
			assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
					image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
					originalImage));

		} catch (Exception e) {
			throw e;
		} finally {
			if (scon != null && !scon.isClosed())
				scon.close();
		}
	}
}
