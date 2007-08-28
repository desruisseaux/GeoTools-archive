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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.arcsde.data.ArcSDEQueryTest;
import org.geotools.arcsde.gce.ArcSDEPyramid;
import org.geotools.arcsde.gce.RasterQueryInfo;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.DataSourceException;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.BoundingBox;

import com.esri.sde.sdk.client.SDEPoint;
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
public class ArcSDEPyramidTest extends TestCase {

    private static Logger LOGGER = Logger.getLogger("org.geotools.arcsde.gce");
    private ArcSDEConnectionPool pool;
    private Properties conProps;

    public ArcSDEPyramidTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        conProps = new Properties();
        InputStream in = org.geotools.test.TestData.url(null, "raster-testparams.properties").openStream();
        conProps.load(in);
        in.close();
        pool = ArcSDEConnectionPoolFactory.getInstance().createPool(new ArcSDEConnectionConfig(conProps));
        
    }

    public void testArcSDEPyramidHypothetical() throws Exception {

        ArcSDEPyramid pyramid = new ArcSDEPyramid(10, 10, 2);
        pyramid.addPyramidLevel(0, new SeExtent(0, 0, 100, 100), null, null, 10, 10, new Dimension(100,100));
        pyramid.addPyramidLevel(1, new SeExtent(0, 0, 100, 100), null, null, 5, 5, new Dimension(50,50));

        RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(0, 10, 0, 10, null),
                0);
        assertTrue(ret.envelope.equals(new ReferencedEnvelope(0, 10, 0, 10, null)));
        assertTrue(ret.image.width == 10 && ret.image.height == 10);

        ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(0, 9, 0, 9, null), 0);
        assertTrue(ret.envelope.intersects((BoundingBox)new ReferencedEnvelope(0, 9, 0, 9, null)));
        assertTrue(ret.image.width == 9 && ret.image.height == 9);

        ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(15, 300, 15, 300, null), 1);
        assertTrue(ret.envelope.equals(new ReferencedEnvelope(14, 300, 14, 300, null)));
        assertTrue(ret.image.width == 143 && ret.image.height == 143);

        ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(-100, 200, -100, 200, null), 1);
        assertTrue(ret.envelope.equals(new ReferencedEnvelope(-100, 200, -100, 200, null)));
        assertTrue(ret.image.width == 150 && ret.image.height == 150);
    }

    public void testArcSDEPyramidThreeBand() throws Exception {

        ArcSDEPooledConnection scon = pool.getConnection();
        SeRasterAttr rAttr;
        try {
            SeQuery q = new SeQuery(scon, new String[] { "RASTER"}, new SeSqlConstruct(conProps.getProperty("threebandtable")));
            q.prepareQuery();
            q.execute();
            SeRow r = q.fetch();
            rAttr = r.getRaster(0);
        } catch (SeException se) {
            scon.close();
            throw new RuntimeException(se.getSeError().getErrDesc(), se);
        }
        
        CoordinateReferenceSystem crs = CRS.decode(conProps.getProperty("tableCRS"));
        ArcSDEPyramid pyramid = new ArcSDEPyramid(rAttr, crs);
        scon.close();
        
        assertTrue(pyramid.getPyramidLevel(0).getYOffset() != 0);
        
        ReferencedEnvelope env = new ReferencedEnvelope(33000.25,48000.225,774000.25,783400.225, crs);
        Rectangle imageSize = new Rectangle(256,128);
        int imgLevel = pyramid.pickOptimalRasterLevel(env,imageSize);
        RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertTrue(imgLevel == 6);
        //LOGGER.info(ret.image + "");
        //LOGGER.info(ret.envelope + "");
        assertTrue(ret.image.equals(new Rectangle(-1,5581,470,295)));
        assertTrue(ret.envelope.contains((BoundingBox)env));
        
        env = new ReferencedEnvelope(40000.0,41001.0,800000.0,801001.0,crs);
        imageSize = new Rectangle(1000,1000);
        imgLevel = pyramid.pickOptimalRasterLevel(env,imageSize);
        ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertTrue(imgLevel == 1);
        //LOGGER.info(ret.image + "");
        //LOGGER.info(ret.envelope + "");
        assertTrue(ret.image.equals(new Rectangle(6999,160999,1002,1002)));
        assertTrue(ret.envelope.contains((BoundingBox)env));

    }

    public void testArcSDEPyramidFourBand() throws Exception {

        ArcSDEPooledConnection scon = pool.getConnection();
        SeRasterAttr rAttr;
        try {
            SeQuery q = new SeQuery(scon, new String[] { "RASTER"}, new SeSqlConstruct(conProps.getProperty("fourbandtable")));
            q.prepareQuery();
            q.execute();
            SeRow r = q.fetch();
            rAttr= r.getRaster(0);
        } catch (SeException se) {
            scon.close();
            throw new RuntimeException(se.getSeError().getErrDesc(), se);
        }
        
        CoordinateReferenceSystem crs = CRS.decode(conProps.getProperty("tableCRS"));
        ArcSDEPyramid pyramid = new ArcSDEPyramid(rAttr, crs);
        scon.close();
        
        assertTrue(pyramid.getPyramidLevel(0).getYOffset() != 0);
        
        ReferencedEnvelope env = new ReferencedEnvelope(33000.25,48000.225,774000.25,783400.225, crs);
        Rectangle imageSize = new Rectangle(256,128);
        int imgLevel = pyramid.pickOptimalRasterLevel(env,imageSize);
        RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertTrue(imgLevel == 6);
        //LOGGER.info(ret.image + "");
        //LOGGER.info(ret.envelope + "");
        assertTrue(ret.image.equals(new Rectangle(-1,5581,470,295)));
        assertTrue(ret.envelope.contains((BoundingBox)env));
        
        env = new ReferencedEnvelope(40000.0,41001.0,800000.0,801001.0,crs);
        imageSize = new Rectangle(1000,1000);
        imgLevel = pyramid.pickOptimalRasterLevel(env,imageSize);
        ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertTrue(imgLevel == 1);
        //LOGGER.info(ret.image + "");
        //LOGGER.info(ret.envelope + "");
        assertTrue(ret.image.equals(new Rectangle(6999,160999,1002,1002)));
        assertTrue(ret.envelope.contains((BoundingBox)env));

    }

}
