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
package org.geotools.renderer.shape;

import java.nio.ByteBuffer;

import org.geotools.data.shapefile.shp.ShapeHandler;
import org.geotools.data.shapefile.shp.ShapeType;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class PointHandler implements ShapeHandler {
	private ShapeType type;
	private Envelope bbox;
	private MathTransform mt;
	private ScreenMap screenMap;

	/**
	 * Create new instance
	 * @param type the type of shape.
	 * @param env the area that is visible.  If shape is not in area then skip.
	 * @param mt the transform to go from data to the envelope (and that should be used to transform the shape coords)
	 * @param hasOpacity 
	 */
	public PointHandler(ShapeType type, Envelope env, MathTransform mt, boolean hasOpacity) 
	throws TransformException {
		this.type=type;
		this.bbox=env;
		this.mt=mt;
		if( mt!=null ){
			screenMap=GeometryHandlerUtilities.calculateScreenSize(env,mt, hasOpacity);
		}
	}
	
	/**
	 * @see org.geotools.data.shapefile.shp.ShapeHandler#getShapeType()
	 */
	public ShapeType getShapeType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.shapefile.shp.ShapeHandler#read(java.nio.ByteBuffer, org.geotools.data.shapefile.shp.ShapeType)
	 */
	public Object read(ByteBuffer buffer, ShapeType type) {
		if (type == ShapeType.NULL) {
            return null;
        }

        double[][] coords = new double[1][];
        coords[0] = new double[]{buffer.getDouble(),buffer.getDouble()};
        double[][] transformed=new double[1][];
        transformed[0]=new double[2];
        Envelope geomBBox = new Envelope(coords[0][0], coords[0][0], coords[0][1], coords[0][1]);
        if( !mt.isIdentity() ){
            try {
                mt.transform(coords[0], 0, transformed[0], 0, 1);
            } catch (Exception e) {
                ShapefileRenderer.LOGGER.severe("could not transform coordinates"
                        + e.getLocalizedMessage());
            }
        }else{
        	transformed=coords;
        }
        
        if( !bbox.intersects(geomBBox) )
            return null;

        if( screenMap.get((int)(transformed[0][0]), (int)transformed[0][1]) ){
        	return null;
        }
        screenMap.set( (int)(transformed[0][0]), (int)(transformed[0][1]), true);
        return new SimpleGeometry(type, transformed, geomBBox);
	}

	/**
	 * @see org.geotools.data.shapefile.shp.ShapeHandler#write(java.nio.ByteBuffer, java.lang.Object)
	 */
	public void write(ByteBuffer buffer, Object geometry) {
		// This handler doesnt write
		throw new UnsupportedOperationException("This handler is only for reading");
	}

	/**
	 * @see org.geotools.data.shapefile.shp.ShapeHandler#getLength(java.lang.Object)
	 */
	public int getLength(Object geometry) {
		// TODO Auto-generated method stub
		return 0;
	}

}
