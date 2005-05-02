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

import com.vividsolutions.jts.geom.Envelope;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class PolygonHandler implements ShapeHandler {

	private ShapeType type;
	private Envelope bbox;
	private MathTransform mt;

	/**
	 * Create new instance
	 * @param type the type of shape.
	 * @param env the area that is visible.  If shape is not in area then skip.
	 * @param mt the transform to go from data to the envelope (and that should be used to transform the shape coords)
	 */
	public PolygonHandler(ShapeType type, Envelope env, MathTransform mt) {
		this.type=type;
		this.bbox=env;
		this.mt=mt;
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
		// TODO Auto-generated method stub
		return null;
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
