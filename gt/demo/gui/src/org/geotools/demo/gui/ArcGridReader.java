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
 * Created on 29-feb-2004
 */
package org.geotools.demo.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.geotools.data.arcgrid.ArcGridDataSource;
import org.geotools.gui.swing.StyledMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.parameter.ParameterGroupDescriptor;
import org.geotools.parameter.ParameterValueGroup;
import org.geotools.styling.ColorMap;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.StyleBuilder;

import org.geotools.data.coverage.grid.GridCoverageExchange;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.coverage.grid.Format;

import org.geotools.gc.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.OperationParameterGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.OperationParameter;
import java.util.Collections;
import java.util.Locale;

//wrap dependancies
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;

import java.awt.geom.Rectangle2D;

/**
 * Simple class that shows how to load and symbolize a DEM from an ascii export 
 * file generated by GRASS
 *
 * TODO: use the url with data from the sample-data jar resource (not hard coded)
 * TODO: do not wrap grid coverage in a feature (needs new MapContext method)
 *
 * @author aaime
 * @author rschulz
 */
public class ArcGridReader {
    public static void main(String[] args) throws Exception {
        
        //the GCE does not like this url
        //URL url = ArcGridReader.class.getClassLoader().getResource("org/geotools/sampleData/spearfish_dem.asc.gz");
        URL url = new URL("file:/home/rschulz/GIS/gt2/tutorial/mapTutorial/TESTGT2/org/geotools/sampleData/spearfish_dem.asc");
        Format f = new org.geotools.data.arcgrid.ArcGridFormat();  //GridFormatFinder.findFormat(url);  //does not work
        GridCoverageReader reader = f.getReader(url);
        
        //get the parameters and set them
        OperationParameterGroup paramDescriptor = f.getReadParameters();
        ParameterValueGroup params = (ParameterValueGroup) paramDescriptor.createValue();
        
        params.getValue( "Compressed" ).setValue( true ); //zipped files do not work
        params.getValue( "GRASS" ).setValue( true );
        
        //read the grid
        if (reader.hasMoreGridCoverages()) {
            System.out.println("Reader has a GC to read");
        }
        //should call reader.hasMoreGridCoverages()
        GridCoverage gc = reader.read( params );
        
        //wrap grid coverage in a Feature in a FeatureCollection
        FeatureCollection fc = FeatureCollections.newCollection();
        fc.add(wrapGcInFeature(gc));

        MapContext mc = new DefaultMapContext();
        StyleBuilder sb = new StyleBuilder();
        ColorMap colorMap = sb.createColorMap(new double[] { 1000, 1300, 1600, 1900 },
                new Color[] {
                    new Color(0, 100, 0), new Color(150, 150, 50), new Color(200, 200, 50),
                    Color.WHITE
                }, ColorMap.TYPE_RAMP);
        RasterSymbolizer rs = sb.createRasterSymbolizer(colorMap, 1.0);
        mc.addLayer(fc, sb.createStyle(rs));

        StyledMapPane mapPane = new StyledMapPane();
        mapPane.setMapContext(mc);

        JFrame frame = new JFrame();
        frame.setContentPane(mapPane.createScrollPane());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(640, 480));
        frame.setTitle("Spearfish DEM");
        frame.show();
        
    }
    
    /**
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource.
     *
     * @param gc the grid coverage
     *
     * @return a feature with the grid coverage envelope as the geometry and the grid coverage
     *         itself in the "grid" attribute
     *
     * @throws IllegalAttributeException Should never be thrown
     * @throws SchemaException Should never be thrown
     */
    private static Feature wrapGcInFeature(GridCoverage gc)
            throws IllegalAttributeException, SchemaException {
        // create surrounding polygon
        PrecisionModel pm = new PrecisionModel();
        CoordinateSequenceFactory csf = DefaultCoordinateSequenceFactory.instance();
        GeometryFactory gf = new GeometryFactory(pm, 0);
        Coordinate[] coord = new Coordinate[5];
        Rectangle2D rect = gc.getEnvelope().toRectangle2D();
        coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
        coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
        coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
        coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
        coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
        
        LinearRing ring = new LinearRing(csf.create(coord), gf);
        Polygon bounds = new Polygon(ring, null, gf);
        
        // create the feature type
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Polygon.class);
        AttributeType grid = AttributeTypeFactory.newAttributeType("grid", GridCoverage.class);
        
        FeatureType schema = null;
        AttributeType[] attTypes = {geom, grid};
//Fix the schema name        
        schema = FeatureTypeFactory.newFeatureType(attTypes, "SomeName");
        
        // create the feature
        Feature feature = schema.create(new Object[] {bounds, gc});
        
        return feature;
    }
}
