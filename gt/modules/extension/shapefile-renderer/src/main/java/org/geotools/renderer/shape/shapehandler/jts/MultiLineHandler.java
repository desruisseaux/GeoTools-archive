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
package org.geotools.renderer.shape.shapehandler.jts;

import java.awt.Rectangle;

import org.geotools.data.shapefile.shp.ShapeType;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;


/**
 * Creates Geometry line objects for use by the ShapeRenderer.
 *
 * @author jeichar
 *
 * @since 2.1.x
 * @source $URL$
 */
public class MultiLineHandler extends org.geotools.renderer.shape.shapehandler.simple.MultiLineHandler {
    static final GeometryFactory factory=new GeometryFactory();
    
    public MultiLineHandler(ShapeType type, Envelope env,
            MathTransform mt, boolean hasOpacity, Rectangle screenSize) throws TransformException {
        super(type, env, mt, hasOpacity, screenSize);
    }
    
    protected Object createGeometry(ShapeType type, Envelope geomBBox, double[][] transformed) {
        
        LineString[] ls=new LineString[transformed.length];
        for (int i = 0; i < transformed.length; i++) {
            ls[i]=factory.createLineString(PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(transformed[i], 2));
        }
        
        return factory.createMultiLineString(ls);
    }
}
