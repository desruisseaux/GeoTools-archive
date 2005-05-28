/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.renderer.lite;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.TestData;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;

import com.vividsolutions.jts.geom.Envelope;

public class GridCoverageRendererTest extends TestCase {
    
    String FILENAME="TestGridCoverage.jpg";
    private static final long TIMEOUT = 3000;
    
    public void testPaint() throws Exception {
       
        FileInputStream inTest = new FileInputStream(TestData.file(this, FILENAME));

        BufferedImage imageTest = ImageIO.read(inTest);

        GridCoverage2D coverage=new GridCoverage2D("GridCoverage",imageTest, DefaultGeographicCRS.WGS84,
                new Envelope2D(DefaultGeographicCRS.WGS84, 0,0, imageTest.getWidth(), imageTest.getHeight()));  
        
        MapContext context=new DefaultMapContext();
        context.addLayer(coverage, getStyle());
        LiteRenderer2 renderer=new LiteRenderer2(context);
        org.opengis.spatialschema.geometry.Envelope geoapiEnv=coverage.getEnvelope();
        Envelope env = new Envelope(geoapiEnv.getMinimum(0), geoapiEnv.getMaximum(0),
                geoapiEnv.getMinimum(1), geoapiEnv.getMaximum(1));
        int boundary=10;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
                env.getMinY() - boundary, env.getMaxY() + boundary);
//        Rendering2DTest.INTERACTIVE=true;
        Rendering2DTest.showRender("testGridCoverage", renderer, TIMEOUT, env);

    }

    private Style getStyle() {
        StyleBuilder sb = new StyleBuilder();
        Style rasterstyle = sb.createStyle();
        RasterSymbolizer raster = sb.createRasterSymbolizer();

        rasterstyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(raster));
        rasterstyle.getFeatureTypeStyles()[0].setFeatureTypeName("GridCoverage");
        return rasterstyle;
    }

}
