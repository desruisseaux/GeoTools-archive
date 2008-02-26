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

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.arcsde.gce.band.ArcSDERasterBandCopier;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters
 * from an ArcSDE database.
 * 
 * This class in particular tests the class which reads data from the underlying
 * raster tile and copies it to a java.awt.Raster for display.
 * 
 * @author Saul Farber
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/gce/UnsignedByteBandCopierTest.java $
 * @version $Id: UnsignedByteBandCopierTest.java 27856 2007-11-12 17:23:35Z
 *          desruisseaux $
 */
public class UnsignedByteBandCopierTest extends TestCase {

    private static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.gce");

    private ArcSDEConnectionPool pool;

    private Properties conProps;

    public UnsignedByteBandCopierTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        conProps = new Properties();
        InputStream in = org.geotools.test.TestData.url(null, "raster-testparams.properties")
                .openStream();
        conProps.load(in);
        in.close();
        pool = ArcSDEConnectionPoolFactory.getInstance().createPool(
                new ArcSDEConnectionConfig(conProps));
    }

    public void testLiveRasterTile() throws Exception {
        ArcSDEPooledConnection scon = null;
        try {

            scon = pool.getConnection();
            SeQuery q = new SeQuery(scon, new String[] { "RASTER" }, new SeSqlConstruct(conProps
                    .getProperty("fourbandtable")));
            q.prepareQuery();
            q.execute();
            SeRow r = q.fetch();
            SeRasterAttr rAttr = r.getRaster(0);

            int[] bands = new int[] { 1, 2, 3 };
            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setBands(bands);
            rConstraint.setLevel(10);
            // pick #bands random tiles in the middle of the state
            rConstraint.setEnvelope(1, 1, 1, 1);
            rConstraint.setInterleave(SeRaster.SE_RASTER_INTERLEAVE_BSQ);

            q.queryRasterTile(rConstraint);

            BufferedImage outputImage = new BufferedImage(128, 128, BufferedImage.TYPE_3BYTE_BGR);
            ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(rAttr
                    .getPixelType(), rAttr.getTileWidth(), rAttr.getTileHeight());

            SeRasterTile rTile = r.getRasterTile();
            for (int i = 0; i < bands.length; i++) {
                LOGGER.info("copying band " + rTile.getBandId().longValue());
                bandCopier
                        .copyPixelData(rTile, outputImage.getRaster(), 0, 0, bands.length - i - 1);
                rTile = r.getRasterTile();
            }

            // ImageIO.write(outputImage, "PNG", new
            // File("ubbCopierTest1.png"));

            // Well, now we have an image tile. Does it have what we expect on
            // it?
            assertTrue("Image from SDE isn't what we expected.", RasterTestUtils.imageEquals(
                    outputImage, conProps.getProperty("unsignedbytebandcopiertest.image")));

        } catch (SeException se) {
            LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
        } finally {
            if (scon != null)
                scon.close();
        }
    }
}
