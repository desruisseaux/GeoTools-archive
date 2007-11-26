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
import java.net.URL;
import java.util.Properties;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters from an
 * ArcSDE database
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEGCReaderWorkingTest extends TestCase {

    private CoordinateReferenceSystem crs;
    private GridGeometry2D statewideRealWorldExampleRes, southShoreExampleRes;
    
    private Properties conProps;

    /**
     * Creates a new ArcSDEConnectionPoolTest object.
     * 
     */
    public ArcSDEGCReaderWorkingTest(String name) throws Exception {
        super(name);
        
        conProps = new Properties();
        String propsFile = "liverasterdatasets.properties";
        URL conParamsSource = org.geotools.test.TestData.url(new ArcSDEPyramidTest(""), propsFile);

        InputStream in = conParamsSource.openStream();
        if (in == null) {
            throw new IllegalStateException("cannot find test params: " + conParamsSource.toExternalForm());
        }
        conProps.load(in);
        in.close();
    }
    
    public void setUp() throws Exception {
        super.setUp();
        
        crs = CRS.decode("EPSG:26986");
        // 33,000.25 m 782,500.143 m , 332,999.75 m 953,499.857 m], java.awt.Rectangle[x=0,y=0,width=500,height=285]
        statewideRealWorldExampleRes = new GridGeometry2D(new GeneralGridRange(new Rectangle(500,285)), new ReferencedEnvelope(33000.25,332999.75,782500.143,953499.857,crs));
        
        // x=0,y=0,width=500,height=285]  envelope -- [222,175.135 m 800,289.513 m , 294,775.014 m 841,671.444 m]
        southShoreExampleRes = new GridGeometry2D(new GeneralGridRange(new Rectangle(500,285)), new ReferencedEnvelope(222175.135,294775.014,800289.513,841671.444,crs));
            
        
    }

    public void testWorkingExample() throws Exception {

        String fourbandurl = conProps.getProperty("fourbandurl");
        
        GridCoverage2D gc;
        Format f = new ArcSDERasterFormatFactory().createFormat();
        AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader)((AbstractGridFormat)f).getReader(fourbandurl);
        
        GeneralParameterValue[] requestParams = new Parameter[1];
        
        //requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D,statewideRealWorldExampleRes);
        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D,southShoreExampleRes);
        gc = (GridCoverage2D)r.read(requestParams);
        assertNotNull(gc);
        try {
            ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new File("workingTestOutput1.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
