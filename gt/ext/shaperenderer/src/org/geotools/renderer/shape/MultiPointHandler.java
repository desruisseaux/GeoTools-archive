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
public class MultiPointHandler implements ShapeHandler {
	private ShapeType type;
	private Envelope bbox;
	private MathTransform mt;

	/**
	 * Create new instance
	 * @param type the type of shape.
	 * @param env the area that is visible.  If shape is not in area then skip.
	 * @param mt the transform to go from data to the envelope (and that should be used to transform the shape coords)
	 */
	public MultiPointHandler(ShapeType type, Envelope env, MathTransform mt) {
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
		if (type == ShapeType.NULL) {
            return null;
        }

        int dimensions = (type == ShapeType.MULTIPOINTZ) ? 3 : 2;
        double[] tmpbbox=new double[4];
        tmpbbox[0] = buffer.getDouble();
        tmpbbox[1]= buffer.getDouble();
        tmpbbox[2]= buffer.getDouble();
        tmpbbox[3]= buffer.getDouble();
        
        if( !mt.isIdentity())
			try {
				mt.transform(tmpbbox,0,tmpbbox, 0, tmpbbox.length/2);
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        Envelope geomBBox = new Envelope(tmpbbox[0], tmpbbox[2], tmpbbox[1], tmpbbox[3]);

        if (!bbox.intersects(geomBBox)) {
            skipMultiPointGeom(buffer, dimensions);
            return null;
        }

        int numpoints = buffer.getInt();
        double[][] coords = new double[numpoints][];
        for( int t = 0; t < numpoints; t++ ) {
            coords[t] = new double[]{buffer.getDouble(), buffer.getDouble()};
            if( !mt.isIdentity() ){
	            try {
	                mt.transform(coords[t], 0, coords[t], 0, coords[t].length/2);
	            } catch (Exception e) {
	                ShapeRenderer.LOGGER.severe("could not transform coordinates"
	                        + e.getLocalizedMessage());
	            }
            }
        }
        if (type == ShapeType.MULTIPOINTZ) {
            buffer.position(buffer.position() + 2 * 8 + numpoints * 8);
        }

        return new SimpleGeometry(type, coords, geomBBox);
    }

    private void skipMultiPointGeom( ByteBuffer buffer, int dimensions ) {

        int numpoints = buffer.getInt();
        // skip x y
        buffer.position(buffer.position() + numpoints * 16);
        // skip z
        if (dimensions == 3)
            buffer.position(buffer.position() + numpoints * 8);
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
