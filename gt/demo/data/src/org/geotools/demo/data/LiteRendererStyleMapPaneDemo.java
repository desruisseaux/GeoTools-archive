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
package org.geotools.demo.data;

import java.awt.Color;

import javax.swing.JFrame;

import org.geotools.data.FeatureSource;
import org.geotools.data.shape.ShapefileDataStore;
import org.geotools.gui.swing.LiteRendererStyledMapPane;
import org.geotools.gui.swing.ZoomPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.resources.TestData;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;

/**
 *  summary sentence.
 * <p>
 * Paragraph ...
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>
 * <li>
 * </ul>
 * </p><p>
 * Example:<pre><code>
 * LiteRendererStyleMapPaneDemo x = new LiteRendererStyleMapPaneDemo( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author jeichar
 * @since 0.9.0
 */
public class LiteRendererStyleMapPaneDemo {

	public static void main(String[] args) throws Exception {

		ShapefileDataStore ds=new ShapefileDataStore(TestData.getResource(LiteRendererStyleMapPaneDemo.class,"bc_2m_border.shp"), true, true);
		FeatureSource featureSource=ds.getFeatureSource();
		
		MapContext context=new DefaultMapContext();
        
        MapLayer[] layers;

        layers = new MapLayer[3];
        
        layers[0] = new DefaultMapLayer(featureSource, createLineStyle(featureSource.getSchema().getTypeName(), Color.BLUE), featureSource.getSchema().getTypeName());

        ds=new ShapefileDataStore(TestData.getResource(LiteRendererStyleMapPaneDemo.class,"bc_2m_lakes.shp"), true, true);
		featureSource=ds.getFeatureSource();
        layers[1] = new DefaultMapLayer(featureSource, createLineStyle(featureSource.getSchema().getTypeName(), Color.GREEN), featureSource.getSchema().getTypeName());
		
		ds=new ShapefileDataStore(TestData.getResource(LiteRendererStyleMapPaneDemo.class,"bc_2m_rivers.shp"), true, true);
		featureSource=ds.getFeatureSource();
        layers[2] = new DefaultMapLayer(featureSource, createLineStyle(featureSource.getSchema().getTypeName(), Color.RED), featureSource.getSchema().getTypeName());
		
		
        context = new DefaultMapContext(layers);
        
        LiteRendererStyledMapPane pane=new LiteRendererStyledMapPane(ZoomPane.UNIFORM_SCALE);
        pane.add(layers[0], 0,false);
        pane.add(layers[1], 1,true);
        pane.add(layers[2], 2,true);

        JFrame frame=new JFrame("test");
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400,400);
        frame.setVisible(true);

	}
	

	public static Style createLineStyle(String typeName, Color color) {
        StyleBuilder sb = new StyleBuilder();
        Style linestyle = sb.createStyle();

        LineSymbolizer line = sb.createLineSymbolizer(color);
        linestyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(line));

        linestyle.getFeatureTypeStyles()[0].setFeatureTypeName(typeName);
        return linestyle;
    }
	
}
