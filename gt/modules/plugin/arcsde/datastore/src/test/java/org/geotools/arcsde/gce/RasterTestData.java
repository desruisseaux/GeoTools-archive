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
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;

import org.geotools.arcsde.data.TestData;
import org.geotools.arcsde.gce.producer.ArcSDERasterOneBitPerBandProducerImpl;
import org.geotools.arcsde.gce.producer.ArcSDERasterOneBytePerBandProducerImpl;
import org.geotools.arcsde.gce.producer.ArcSDERasterProducer;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.pe.PeFactory;
import com.esri.sde.sdk.pe.PePCSDefs;
import com.esri.sde.sdk.pe.PeProjectedCS;

public class RasterTestData {

    private TestData testData;

    private Properties conProps;

    private Logger LOGGER = Logging.getLogger(this.getClass());

    public void setUp() throws IOException {
        // load a raster dataset into SDE
        testData = new TestData();
        testData.setUp();

        conProps = new Properties();
        String propsFile = "raster-testparams.properties";
        URL conParamsSource = org.geotools.test.TestData.url(null, propsFile);

        InputStream in = conParamsSource.openStream();
        if (in == null) {
            throw new IllegalStateException("cannot find test params: "
                    + conParamsSource.toExternalForm());
        }
        conProps.load(in);
        in.close();
    }

    public TestData getTestData() {
        return testData;
    }

    /*
     * Names for the raster data test tables
     */
    public String get1bitRasterTableName() throws SeException,
            UnavailableArcSDEConnectionException, DataSourceException {
        return testData.getTemp_table() + "_ONEBITRASTER";
    }

    public String getRGBRasterTableName() throws SeException, UnavailableArcSDEConnectionException,
            DataSourceException {
        return testData.getTemp_table() + "_RGBRASTER";
    }

    public String getRGBARasterTableName() throws SeException,
            UnavailableArcSDEConnectionException, DataSourceException {
        return testData.getTemp_table() + "_RGBARASTER";
    }

    public String getRGBColorMappedRasterTableName() throws SeException,
            UnavailableArcSDEConnectionException, DataSourceException {
        return testData.getTemp_table() + "_RGBRASTER_CM";
    }

    public String getGrayScaleOneByteRasterTableName() throws SeException,
            UnavailableArcSDEConnectionException, DataSourceException {
        return testData.getTemp_table() + "_GRAYSCALERASTER";
    }

    public String getRasterTestDataProperty(String propName) {
        return conProps.getProperty(propName);
    }

    /**
     * Loads the 1bit raster test data into the table given in
     * {@link RasterTestData#get1bitRasterTableName()}
     * 
     * @throws Exception
     */
    public void load1bitRaster() throws Exception {
        // we're definitely piggybacking on the testData class here
        ArcSDEPooledConnection conn = testData.getConnectionPool().getConnection();
        final String tableName = get1bitRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);
        // build the base business table. We'll add the raster data to it in a bit
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(231000, 898000, 231000 + 500, 898000 + 500);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = conProps.getProperty("sampledata.onebitraster");
        ArcSDERasterProducer producer = new ArcSDERasterOneBitPerBandProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_1BIT, imgExtent,
                producer);
    }

    /**
     * Loads the 1bit raster test data into the table given in
     * {@link RasterTestData#get1bitRasterTableName()}
     * 
     * @throws Exception
     */
    public void loadRGBRaster() throws Exception {
        // we're definitely piggybacking on the testData class here
        ArcSDEPooledConnection conn = testData.getConnectionPool().getConnection();
        final String tableName = getRGBRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);
        // build the base business table. We'll add the raster data to it in a bit
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(231000, 898000, 231000 + 500, 898000 + 500);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = conProps.getProperty("sampledata.rgbraster");
        ArcSDERasterProducer prod = new ArcSDERasterOneBytePerBandProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_8BIT_U, imgExtent,
                prod);
    }

    public void loadRGBColorMappedRaster() throws Exception {
        // Note that this DOESN'T LOAD THE COLORMAP RIGHT NOW.
        ArcSDEPooledConnection conn = testData.getConnectionPool().getConnection();
        final String tableName = getRGBColorMappedRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);
        // build the base business table. We'll add the raster data to it in a bit
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(231000, 898000, 231000 + 500, 898000 + 500);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = conProps.getProperty("sampledata.rgbraster-colormapped");
        ArcSDERasterProducer prod = new ArcSDERasterOneBytePerBandProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_8BIT_U, imgExtent,
                prod);
    }

    public void loadOneByteGrayScaleRaster() throws Exception {
        // Note that this DOESN'T LOAD THE COLORMAP RIGHT NOW.
        ArcSDEPooledConnection conn = testData.getConnectionPool().getConnection();
        final String tableName = getGrayScaleOneByteRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);
        // build the base business table. We'll add the raster data to it in a bit
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(231000, 898000, 231000 + 500, 898000 + 500);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = conProps.getProperty("sampledata.onebyteonebandraster");
        ArcSDERasterProducer prod = new ArcSDERasterOneBytePerBandProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_8BIT_U, imgExtent,
                prod);
    }

    public SeCoordinateReference getSeCRSFromPeProjectedCSId(int PeProjectedCSId) {
        SeCoordinateReference crs;
        try {
            PeProjectedCS pcs = (PeProjectedCS) PeFactory.factory(PeProjectedCSId);
            crs = new SeCoordinateReference();
            crs.setCoordSysByDescription(pcs.toString());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return crs;
    }

    public void createRasterBusinessTempTable(String tableName, ArcSDEPooledConnection conn)
            throws Exception {

        SeColumnDefinition[] colDefs = new SeColumnDefinition[1];
        SeTable table = new SeTable(conn, tableName);

        // first column to be SDE managed feature id
        colDefs[0] = new SeColumnDefinition("ROW_ID", SeColumnDefinition.TYPE_INTEGER, 10, 0, false);
        conn.getLock().lock();
        table.create(colDefs, testData.getConfigKeyword());
        conn.getLock().unlock();

        /*
         * Register the column to be used as feature id and managed by sde
         */
        SeRegistration reg = new SeRegistration(conn, table.getName());
        LOGGER.fine("setting rowIdColumnName to ROW_ID in table " + reg.getTableName());
        reg.setRowIdColumnName("ROW_ID");
        final int rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE;
        reg.setRowIdColumnType(rowIdColumnType);
        conn.getLock().lock();
        reg.alter();
        conn.getLock().unlock();
    }

    public void importRasterImage(final String tableName,
            SeCoordinateReference crs,
            final String rasterFilename,
            final int sePixelType,
            SeExtent extent,
            ArcSDERasterProducer prod) throws Exception {
        importRasterImage(tableName, crs, rasterFilename, sePixelType, extent, prod, null);
    }

    public void importRasterImage(final String tableName,
            SeCoordinateReference crs,
            final String rasterFilename,
            final int sePixelType,
            SeExtent extent,
            ArcSDERasterProducer prod,
            IndexColorModel colorModel) throws Exception {
        ArcSDEPooledConnection conn = testData.getConnectionPool().getConnection();
        try {

            // much of this code is from
            // http://edndoc.esri.com/arcsde/9.2/concepts/rasters/dataloading/dataloading.htm
            SeRasterColumn rasCol = new SeRasterColumn(conn);
            rasCol.setTableName(tableName);
            rasCol.setDescription("Sample geotools ArcSDE raster test-suite data.");
            rasCol.setRasterColumnName("RASTER");
            rasCol.setCoordRef(crs);
            rasCol.setConfigurationKeyword(testData.getConfigKeyword());

            conn.getLock().lock();
            rasCol.create();
            conn.getLock().unlock();

            // now start loading the actual raster data
            BufferedImage sampleImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasterFilename));

            int imageWidth = sampleImage.getWidth(), imageHeight = sampleImage.getHeight();

            SeRasterAttr attr = new SeRasterAttr(true);
            attr.setImageSize(imageWidth, imageHeight, sampleImage.getSampleModel().getNumBands());
            attr.setTileSize(128, 128);
            attr.setPixelType(sePixelType);
            attr.setCompressionType(SeRaster.SE_COMPRESSION_NONE);
            // no pyramiding
            // attr.setPyramidInfo(3, true, SeRaster.SE_INTERPOLATION_BILINEAR);
            attr.setMaskMode(false);
            attr.setImportMode(false);

            attr.setExtent(extent);
            // attr.setImageOrigin();

            prod.setSeRasterAttr(attr);
            prod.setSourceImage(sampleImage);
            attr.setRasterProducer(prod);

            try {
                SeInsert insert = new SeInsert(conn);
                insert.intoTable(tableName, new String[] { "RASTER" });
                // no buffered writes on raster loads
                insert.setWriteMode(false);
                SeRow row = insert.getRowToSet();
                row.setRaster(0, attr);

                conn.getLock().lock();
                insert.execute();
                insert.close();
                conn.getLock().unlock();
            } catch (SeException se) {
                se.printStackTrace();
                throw se;
            }

            // if there's a colormap to insert, let's add that too
            if (colorModel != null) {
                attr = getRasterAttributes(tableName, new Rectangle(0, 0, 0, 0), 0, new int[] { 1 });
                // attr.getBands()[0].setColorMap(SeRaster.SE_COLORMAP_DATA_BYTE, );
                // NOT IMPLEMENTED FOR NOW!
            }

        } finally {
            conn.close();
        }
    }

    public void tearDown() throws Exception {
        // destroy all sample tables;
        testData.deleteTable(get1bitRasterTableName());
        testData.deleteTable(getRGBRasterTableName());
        testData.deleteTable(getRGBARasterTableName());
        testData.deleteTable(getGrayScaleOneByteRasterTableName());
        testData.deleteTable(getRGBColorMappedRasterTableName());
    }

    /**
     * convenience method to test if two images are identical in their RGB pixel values
     * 
     * @param image
     * @param fileName
     * @return
     * @throws IOException
     */
    public static boolean imageEquals(RenderedImage image, String fileName) throws IOException {
        InputStream in = org.geotools.test.TestData.url(null, fileName).openStream();
        BufferedImage expected = ImageIO.read(in);

        return imageEquals(image, expected);
    }

    /**
     * convenience method to test if two images are identical in their RGB pixel values
     * 
     * @param image1
     * @param image2
     * @return
     */
    public static boolean imageEquals(RenderedImage image1, RenderedImage image2) {
        BufferedImage img1Buff = PlanarImage.wrapRenderedImage(image1).getAsBufferedImage();
        BufferedImage img2Buff = PlanarImage.wrapRenderedImage(image2).getAsBufferedImage();
        for (int xpos = 0; xpos < image1.getWidth(); xpos++) {
            for (int ypos = 0; ypos < image1.getHeight(); ypos++) {
                if (img1Buff.getRGB(xpos, ypos) != img2Buff.getRGB(xpos, ypos)) {
                    System.out.println("pixel " + xpos + "," + ypos + " isn't identical");
                    return false;
                }
            }
        }
        return true;
    }

    public SeRasterAttr getRasterAttributes(final String rasterName,
            Rectangle tiles,
            int level,
            int[] bands) throws DataSourceException, UnavailableArcSDEConnectionException {

        ArcSDEPooledConnection conn = testData.getConnectionPool().getConnection();

        try {
            SeQuery query = new SeQuery(conn, new String[] { conn.getRasterColumn(rasterName)
                    .getName() }, new SeSqlConstruct(rasterName));
            conn.getLock().lock();
            query.prepareQuery();
            query.execute();
            final SeRow r = query.fetch();
            conn.getLock().unlock();

            // Now build a SeRasterConstraint object which queries the db for
            // the right tiles/bands/pyramid level
            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setEnvelope((int) tiles.getMinX(), (int) tiles.getMinY(), (int) tiles
                    .getMaxX(), (int) tiles.getMaxY());
            rConstraint.setLevel(level);
            rConstraint.setBands(bands);

            // Finally, execute the raster query aganist the already-opened
            // SeQuery object which already has an SeRow fetched against it.
            conn.getLock().lock();

            query.queryRasterTile(rConstraint);
            final SeRasterAttr rattr = r.getRaster(0);

            query.close();
            conn.getLock().unlock();

            return rattr;
        } catch (SeException se) {
            throw new DataSourceException(se);
        } finally {
            conn.close();
        }
    }
}
