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
/*
 * ArcGridStatTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */
package org.geotools.gce.arcgrid;

import org.geotools.coverage.grid.GridCoverageImpl;
import org.geotools.data.DataSourceException;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.filter.Filter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.resources.TestData;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;

import java.awt.image.Raster;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.reflect.Array;

import java.net.URL;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * DOCUMENT ME!
 *
 * @author Christiaan ten Klooster
 */
public class ArcGridRenderTest extends TestCaseSupport {
    private static boolean setup = false;
    private static String dataFolder;
    private GridCoverageReader reader;

    public ArcGridRenderTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite(ArcGridRenderTest.class));
    }

    public void setUp() throws Exception {
        if (setup) {
            return;
        }

        setup = true;

        // Build the coordinate system
        //        DatumType.Local type = (DatumType.Local) DatumType.getEnum(DatumType.Local.MINIMUM);
        //        LocalDatum ld = CoordinateSystemFactory.getDefault().createLocalDatum("", type);
        //        AxisInfo[] ai = { AxisInfo.X, AxisInfo.Y };
        //
        //        CoordinateSystem cs = CoordinateSystemFactory.getDefault().createLocalCoordinateSystem("RD",
        //                ld, Unit.METRE, ai);
        //ds.setCoordinateSystem(cs);
        URL url = TestData.getResource(this, "ArcGrid.asc");
        ArcGridFormat format = new ArcGridFormat();

        assertTrue("Unabled to accept:" + url, format.accepts(url));
        reader = new ArcGridReader(url);

        System.out.println("get a reader " + reader);
    }

    public void testRenderImage()
        throws Exception {
        renderImage("renderedArcGrid.jpg");
    }

    private void renderImage(String filename)
        throws DataSourceException, FactoryConfigurationError, 
            FileNotFoundException, IOException {
        Filter filter = null;

        //FeatureCollection ft = ds.getFeatures(filter);
        MapContext mapContext = new DefaultMapContext();
        StyleFactory sFac = StyleFactory.createStyleFactory();

        Format format = reader.getFormat();

        ParameterValueGroup params = format.getReadParameters();

        GeneralParameterValue[] gpvs = new GeneralParameterValue[params.values()
                                                                       .size()];

        for (int i = 0; i < params.values().size(); i++) {
            gpvs[i] = (GeneralParameterValue) params.values().get(i);
        }

        GridCoverage gc = reader.read(gpvs);

        //        Raster raster = ((GridCoverageImpl)gc).getRenderedImage().getData();
        Envelope ex1 = gc.getEnvelope();
        DirectPosition p1 = ex1.getLowerCorner();
        DirectPosition p2 = ex1.getUpperCorner();

        //        Envelope ex = new GeneralEnvelope( p1.getOrdinate( 0 ), p1.getOrdinate( 1 ), p2.getOrdinate( 0 ), p2.getOrdinate( 1 ) );
        //The following is complex, and should be built from
        //an SLD document and not by hand
        RasterSymbolizer rs = sFac.getDefaultRasterSymbolizer();
        Rule rule = sFac.createRule();

        rule.setSymbolizers(new Symbolizer[] { rs });

        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[] { rule });
        Style style = sFac.createStyle();

        style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts });

        MapLayer mapLayer;

        // TODO: get lite renderer to support GC natively
        // mapContext.addLayer(ft, style);        
        //ArcGridRaster arcGridRaster = ds.openArcGridRaster();
        //arcGridRaster.parseHeader();
        //int w = arcGridRaster.getNCols();
        //int h = arcGridRaster.getNRows();
        //        int w = raster.getWidth();
        //        int h = raster.getWidth();

        /*
        LiteRenderer renderer = new LiteRenderer(mapContext);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);

        Rectangle paintArea = new Rectangle(w, h);
        Envelope dataArea = mapContext.getLayerBounds();
        AffineTransform at = renderer.worldToScreenTransform(dataArea, paintArea);
        renderer.paint(g, paintArea, at);

        java.net.URL base = TestData.getResource( this, null );
        File file = new File(java.net.URLDecoder.decode( base.getPath(), "UTF-8"), filename);
        System.out.println("Writing to " + file.getAbsolutePath());

        FileOutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "JPEG", out);*/
    }
}
