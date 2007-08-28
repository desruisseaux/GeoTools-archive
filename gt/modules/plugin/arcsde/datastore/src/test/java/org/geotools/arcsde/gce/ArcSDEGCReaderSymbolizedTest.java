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
import java.awt.image.RenderedImage;
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
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerSupport;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
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
public class ArcSDEGCReaderSymbolizedTest extends TestCase {

    private CoordinateReferenceSystem crs;
    private GridGeometry2D realWordlExampleRes;
    private RasterSymbolizer symbolizer;
    
    private Properties conProps;

    /**
     * Creates a new ArcSDEConnectionPoolTest object.
     * 
     */
    public ArcSDEGCReaderSymbolizedTest(String name) throws Exception {
        super(name);
        
        conProps = new Properties();
        String propsFile = "raster-testparams.properties";
        URL conParamsSource = org.geotools.test.TestData.url(null, propsFile);

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
        realWordlExampleRes = new GridGeometry2D(new GeneralGridRange(new Rectangle(256,128)), new ReferencedEnvelope(33000.25,330000.225,774000.25,983400.225, crs));
        
        InputStream in = org.geotools.test.TestData.url(null, "testRasterStyle.sld.xml").openStream();
        SLDParser parser = new SLDParser(CommonFactoryFinder.getStyleFactory(GeoTools.getDefaultHints()));
        parser.setInput(in);
        StyledLayerDescriptor sld = parser.parseSLD();
        NamedLayer layerOne = (NamedLayer)sld.getStyledLayers()[0];
        symbolizer = (RasterSymbolizer)layerOne.getStyles()[0].getFeatureTypeStyles()[0].getRules()[0].getSymbolizers()[0];
    }

    public void testRead3BandCoverage() throws Exception {

        String threebandurl = conProps.getProperty("threebandrasterurl");
        String fourbandurl = conProps.getProperty("fourbandrasterurl");
        
        GridCoverage2D gc;
        Format f = new ArcSDERasterFormatFactory().createFormat();
        final RasterSymbolizerSupport rsp = new RasterSymbolizerSupport(symbolizer);
        // a fix
        
        GeneralParameterValue[] requestParams = new Parameter[1];
        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D,realWordlExampleRes);
        
        AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader)((AbstractGridFormat)f).getReader(threebandurl);
        gc = (GridCoverage2D)r.read(requestParams);
        assertNotNull(gc);
        //ImageIO.write(((GridCoverage2D) rsp.recolorCoverage(gc)).geophysics(true).getRenderedImage(), "PNG", new File("threeBandRecolored.png"));
        assertTrue("Image from SDE isn't what we expected.",
                RasterTestUtils.imageEquals(((GridCoverage2D) rsp.recolorCoverage(gc)).geophysics(true).getRenderedImage(),
                        "threeBandRecolored.png"));
        
        r = (AbstractGridCoverage2DReader)((AbstractGridFormat)f).getReader(fourbandurl);
        gc = (GridCoverage2D)r.read(requestParams);
        assertNotNull(gc);
        //ImageIO.write(((GridCoverage2D) rsp.recolorCoverage(gc)).geophysics(false).getRenderedImage(), "PNG", new File("fourBandRecolored.png"));
        assertTrue("Image from SDE isn't what we expected.",
                RasterTestUtils.imageEquals(((GridCoverage2D) rsp.recolorCoverage(gc)).geophysics(true).getRenderedImage(),
                        "fourBandRecolored.png"));
        
        
    }
}
