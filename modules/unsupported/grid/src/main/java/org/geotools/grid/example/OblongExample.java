/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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
 */

package org.geotools.grid.example;

import java.awt.Color;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.grid.AttributeSetter;
import org.geotools.grid.GridElement;
import org.geotools.grid.oblong.Oblongs;
import org.geotools.map.DefaultMapContext;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Demonstrates creating a vector grid of tesselated hexagons and setting
 * the value of an attribute for each grid element based on its position.
 * <p>
 * TODO: move this to the demo/example project later
 *
 * @author mbedward
 */
public class OblongExample {

    public static void main(String[] args) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("oblongs");
        typeBuilder.add("oblong", Polygon.class, (CoordinateReferenceSystem)null);
        typeBuilder.add("color", Color.class);
        final SimpleFeatureType TYPE = typeBuilder.buildFeatureType();

        final Envelope bounds = new Envelope(0, 100, 0, 100);

        AttributeSetter attributeSetter = new AttributeSetter(TYPE) {
            public void setAttributes(GridElement el, Map<String, Object> attributes) {
                int g = (int) (255 * el.getCenter().x / bounds.getWidth());
                int b = (int) (255 * el.getCenter().y / bounds.getHeight());
                attributes.put("color", new Color(0, g, b));
            }
        };

        final double width = 8.0;
        final double height = 4.0;

        SimpleFeatureCollection lattice = Oblongs.createGrid(bounds, width, height, attributeSetter);

        DefaultMapContext map = new DefaultMapContext();
        map.addLayer(lattice, createStyle("color"));
        JMapFrame.showMap(map);
    }

    private static Style createStyle(String propName) {
        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2(null);
        StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

        Stroke stroke = sf.createStroke(ff2.literal(Color.BLACK), ff2.literal(1.0));
        Fill fill = sf.createFill(ff2.property(propName));
        PolygonSymbolizer sym = sf.createPolygonSymbolizer(stroke, fill, null);
        return SLD.wrapSymbolizers(sym);
    }
}