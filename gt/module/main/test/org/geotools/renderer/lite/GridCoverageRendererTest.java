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

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.JTS;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.TestData;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class GridCoverageRendererTest extends TestCase {
    
    String FILENAME="TestGridCoverage.jpg";
    private static final long TIMEOUT = 3000;
    
    public void testPaint() throws Exception {
       
        FileInputStream inTest = new FileInputStream(TestData.file(this, FILENAME));
        BufferedImage imageTest = ImageIO.read(inTest);
        
        GridSampleDimension[] bands=new GridSampleDimension[imageTest.getColorModel().getNumComponents()];
        for (int i = 0; i < bands.length; i++) {
        	bands[i]=new GridSampleDimension();
		}
        int width=imageTest.getWidth(), height=imageTest.getHeight();
        double ratio=((double)width/(double)height);
        Envelope2D gcEnv = new Envelope2D(DefaultGeographicCRS.WGS84, -128,49, 14, 14*ratio);
        GridCoverage2D coverage=new GridCoverage2D("GridCoverage",imageTest, DefaultGeographicCRS.WGS84,
        		gcEnv,
                bands, null, null);  
        
        MapContext context=new DefaultMapContext();
        Style style = getStyle();
        context.addLayer(coverage, style );
        StreamingRenderer renderer=new StreamingRenderer();
        renderer.setContext(context);
        Envelope env = context.getLayerBounds();
        int boundary=1;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
                env.getMinY() - boundary, env.getMaxY() + boundary);
        //Rendering2DTest.INTERACTIVE=true;
        Rendering2DTest.showRender("testGridCoverage", renderer, TIMEOUT, env);
    }    
    
    public void testReproject() throws Exception {
       
    	 FileInputStream inTest = new FileInputStream(TestData.file(this, FILENAME));

         BufferedImage imageTest = ImageIO.read(inTest);
         
         GridSampleDimension[] bands=new GridSampleDimension[imageTest.getColorModel().getNumComponents()];
         for (int i = 0; i < bands.length; i++) {
         	bands[i]=new GridSampleDimension();
 		}
         int width=imageTest.getWidth(), height=imageTest.getHeight();
         double ratio=((double)width/(double)height);
         Envelope2D gcEnv = new Envelope2D(DefaultGeographicCRS.WGS84, -128,49, 14, 14*ratio);
         GridCoverage2D coverage=new GridCoverage2D("GridCoverage",imageTest, DefaultGeographicCRS.WGS84,
         		gcEnv,
                 bands, null, null);  
         
         MapContext context=new DefaultMapContext();
         Style style = getStyle();
         context.addLayer(coverage, style );
         
         CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(
         "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");
         
         context.setAreaOfInterest(context.getLayerBounds(), crs);
         
         StreamingRenderer renderer=new StreamingRenderer();
         renderer.setContext(context);


         Envelope env = context.getLayerBounds();
         int boundary=1;
         
         env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
                 env.getMinY() - boundary, env.getMaxY() + boundary);
         
         env=JTS.transform(env, CRS.transform(DefaultGeographicCRS.WGS84, crs, true), 10);
         //Rendering2DTest.INTERACTIVE=true;
         Rendering2DTest.showRender("testReproject", renderer, TIMEOUT, env);
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
