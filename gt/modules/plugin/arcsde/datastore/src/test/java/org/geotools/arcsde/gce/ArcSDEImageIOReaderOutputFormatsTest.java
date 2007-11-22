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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.geotools.arcsde.data.ArcSDEQueryTest;
import org.geotools.arcsde.gce.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDERasterImageReadParam;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReader;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReaderSpi;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters
 * from an ArcSDE database
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEImageIOReaderOutputFormatsTest extends TestCase {

    private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.arcsde.gce");

    private ArcSDEConnectionPool pool = null;

    private HashMap fourBandReaderProps, threeBandReaderProps;

    private SeRasterAttr rasterAttr;

    /**
     * Creates a new ArcSDEConnectionPoolTest object.
     * 
     */
    public ArcSDEImageIOReaderOutputFormatsTest(String name) throws Exception {
        super(name);   
    }

    /**
     * loads {@code test-data/testparams.properties} to get connection
     * parameters and sets up an ArcSDEConnectionPool
     * 
     * @throws Exception
     *             DOCUMENT ME!
     * @throws IllegalStateException
     *             DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        //do setup one time only
        if (pool != null) return;

        Properties conProps = new Properties();
        String propsFile = "raster-testparams.properties";
        InputStream in = org.geotools.test.TestData.openStream(null, propsFile);

        conProps.load(in);
        in.close();

        ArcSDEConnectionConfig connectionConfig = new ArcSDEConnectionConfig(conProps);
        pool = ArcSDEConnectionPoolFactory.getInstance().createPool(connectionConfig);

        ArcSDEPooledConnection scon = null;
        SeQuery q = null;
        ArcSDEPyramid pyramid;
        SeRow r;
        CoordinateReferenceSystem crs = CRS.decode("EPSG:26986");
        String tableName;
        try {

            // Set up a pyramid and readerprops for the four-band 2005 imagery
            scon = pool.getConnection();
            tableName = conProps.getProperty("fourbandtable");
            q = new SeQuery(scon, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            q.prepareQuery();
            q.execute();
            r = q.fetch();
            rasterAttr = r.getRaster(0);
            pyramid = new ArcSDEPyramid(rasterAttr, crs);

            fourBandReaderProps = new HashMap();
            fourBandReaderProps.put(ArcSDERasterReaderSpi.PYRAMID, pyramid);
            fourBandReaderProps.put(ArcSDERasterReaderSpi.RASTER_TABLE, tableName);
            fourBandReaderProps.put(ArcSDERasterReaderSpi.RASTER_COLUMN, "RASTER");
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
            // Set up a pyramid and readerprops for the three-band 2001 imagery
            scon = pool.getConnection();
            conProps.getProperty("threebandtable");
            q = new SeQuery(scon, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            q.prepareQuery();
            q.execute();
            r = q.fetch();
            rasterAttr = r.getRaster(0);
            pyramid = new ArcSDEPyramid(rasterAttr, crs);

            threeBandReaderProps = new HashMap();
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
    }

    /**
     * closes the connection pool if it's still open
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 =
     * GREEN, 3 = BLUE, 4 = NEAR_INFRARED) into a TYPE_INT_RGB image.
     * 
     * Bands are mapped as follows: rasterband 1 => image band 0 rasterband 2 =>
     * image band 1 rasterband 3 => image band 2
     * 
     */
    public void testRead4BandIntoTYPE_INT_RGBImage() throws Exception {

        String imgPrefix = "type_int_rgb-fourband-image";
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(fourBandReaderProps);

        ArcSDEPooledConnection scon = null;
        try {
            scon = pool.getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            // red band
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            // blue band
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            // green band
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(scon);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"1.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setDestinationOffset(new Point(100, 100));
            reader.read(8, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "2.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"2.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setSourceRegion(new Rectangle(43, 30, 1000, 1000));
            rParam.setDestinationOffset(new Point(0, 0));
            reader.read(8, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "3.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"3.png"));
        } finally {
            if (scon != null && !scon.isClosed())
                scon.close();
        }
    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 =
     * GREEN, 3 = BLUE, 4 = NEAR_INFRARED) into a TYPE_INT_ARGB image.
     * 
     * Bands are mapped as follows: rasterband 1 => image band 1 (red,
     * hopefully!) rasterband 2 => image band 2 (green, hopefully!) rasterband 3 =>
     * image band 3 (blue, hopefully!)
     * 
     * Question: what do we do about image band 0 (the alpha band?) Ignoring it
     * for now.
     * 
     */
    public void testRead4BandIntoTYPE_INT_ARGBImage() throws Exception {

        String imgPrefix = "type_int_argb-fourband-image";
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(fourBandReaderProps);

        ArcSDEPooledConnection scon = null;
        try {
            scon = pool.getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            // red band
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            // blue band
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            // green band
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(scon);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            int[] opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque, image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"1.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque, image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setDestinationOffset(new Point(100, 100));
            reader.read(8, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "2.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"2.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque, image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setSourceRegion(new Rectangle(43, 30, 1000, 1000));
            rParam.setDestinationOffset(new Point(0, 0));
            reader.read(8, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "3.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"3.png"));
        } finally {
            if (scon != null)
                scon.close();
        }
    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 =
     * GREEN, 3 = BLUE, 4 = NEAR_INFRARED) into a TYPE_INT_ARGB image.
     * 
     * Bands are mapped as follows: rasterband 1 => image band 1 (red,
     * hopefully!) rasterband 2 => image band 2 (green, hopefully!) rasterband 3 =>
     * image band 3 (blue, hopefully!)
     * 
     * Question: what do we do about image band 0 (the alpha band?) Ignoring it
     * for now.
     * 
     */
    public void testRead3BandIntoTYPE_INT_RGBImage() throws Exception {

        String imgPrefix = "type_int_rgb-threeband-image";

        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(threeBandReaderProps);

        ArcSDEPooledConnection scon = null;
        try {
            scon = pool.getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            // red band
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            // blue band
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            // green band
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(scon);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"1.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setDestinationOffset(new Point(100, 100));
            reader.read(8, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "2.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"2.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setSourceRegion(new Rectangle(43, 30, 1000, 1000));
            rParam.setDestinationOffset(new Point(0, 0));
            reader.read(8, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "3.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"3.png"));
        } finally {
            if (scon != null)
                scon.close();
        }

    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 =
     * GREEN, 3 = BLUE, 4 = NEAR_INFRARED) into a TYPE_INT_ARGB image.
     * 
     * Bands are mapped as follows: rasterband 1 => image band 1 (red,
     * hopefully!) rasterband 2 => image band 2 (green, hopefully!) rasterband 3 =>
     * image band 3 (blue, hopefully!)
     * 
     * Question: what do we do about image band 0 (the alpha band?) Ignoring it
     * for now.
     * 
     */
    public void testRead3BandIntoTYPE_INT_ARGBImage() throws Exception {

        String imgPrefix = "type_int_argb-threeband-image";

        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(threeBandReaderProps);

        ArcSDEPooledConnection scon = null;
        try {
            scon = pool.getConnection();
            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            // red band
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            // blue band
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            // green band
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(scon);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            int[] opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque, image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"1.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque, image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setDestinationOffset(new Point(100, 100));
            reader.read(8, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "2.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"2.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque, image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setSourceRegion(new Rectangle(43, 30, 1000, 1000));
            rParam.setDestinationOffset(new Point(0, 0));
            reader.read(8, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "3.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"3.png"));
        } finally {
            if (scon != null && !scon.isClosed())
                scon.close();
        }
    }
    
    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 =
     * GREEN, 3 = BLUE, 4 = NEAR_INFRARED) into a TYPE_INT_ARGB image.
     * 
     * Bands are mapped as follows: rasterband 1 => image band 1 (red,
     * hopefully!) rasterband 2 => image band 2 (green, hopefully!) rasterband 3 =>
     * image band 3 (blue, hopefully!)
     * 
     * Question: what do we do about image band 0 (the alpha band?) Ignoring it
     * for now.
     * 
     */
    public void testRead3BandIntoTYPE_3BYTE_BGRImage() throws Exception {

        String imgPrefix = "type_3byte_bgr-3band-image";

        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(threeBandReaderProps);

        ArcSDEPooledConnection scon = null;
        try {
            scon = pool.getConnection();
            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(scon);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"1.png"));
        } catch (Exception e) {
            throw e;
        } finally {
            if (scon != null && !scon.isClosed())
                scon.close();
        }
    }
    
    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 =
     * GREEN, 3 = BLUE, 4 = NEAR_INFRARED) into a TYPE_INT_ARGB image.
     * 
     * Bands are mapped as follows: rasterband 1 => image band 1 (red,
     * hopefully!) rasterband 2 => image band 2 (green, hopefully!) rasterband 3 =>
     * image band 3 (blue, hopefully!)
     * 
     * Question: what do we do about image band 0 (the alpha band?) Ignoring it
     * for now.
     * 
     */
    public void testRead4BandIntoTYPE_3BYTE_BGRImage() throws Exception {

        String imgPrefix = "type_3byte_bgr-4band-image";

        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi().createReaderInstance(fourBandReaderProps);

        ArcSDEPooledConnection scon = null;
        try {
            scon = pool.getConnection();
            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(scon);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_4BYTE_ABGR);
            int[] opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque, image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            //ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.",
                    RasterTestUtils.imageEquals(image,
                            imgPrefix+"1.png"));
        } finally {
            if (scon != null && !scon.isClosed())
                scon.close();
        }
    }
}
