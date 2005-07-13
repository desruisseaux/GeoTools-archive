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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
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
	private ScreenMap screenMap;
	private double spanx;
	private double spany;

	/**
	 * Create new instance
	 * @param type the type of shape.
	 * @param env the area that is visible.  If shape is not in area then skip.
	 * @param mt the transform to go from data to the envelope (and that should be used to transform the shape coords)
	 */
	public MultiPointHandler(ShapeType type, Envelope env, MathTransform mt) 
	throws TransformException {
		this.type=type;
		this.bbox=env;
		this.mt=mt;
		if( mt!=null ){
			double[] worldSize=new double[]{
					env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY()
			};
			double[] screenSize=new double[4];
			mt.transform(worldSize, 0, screenSize, 0, 2);
			int width=(int) (screenSize[1]-screenSize[0]);
			int height=-1*(int) (screenSize[3]-screenSize[2]);
			screenMap=new ScreenMap(width+1,height+1);
			
			MathTransform screenToWorld = mt.inverse();
			double[] original = new double[] { 0, 0, 1, 1 };
			double[] coords = new double[4];
			screenToWorld.transform(original, 0, coords, 0, 2);
			this.spanx = Math.abs(coords[0] - coords[2]);
			this.spany = Math.abs(coords[1] - coords[3]);
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

		// read bounding box
		Envelope geomBBox = GeometryHandlerUtilities.readBounds(buffer);

		if (!bbox.intersects(geomBBox)) {
			return null;
		}

		boolean bboxdecimate = geomBBox.getWidth() <= spanx
				&& geomBBox.getHeight() <= spany;
		int numParts = buffer.getInt();

		double[][] coords= new double[numParts][];
		double[][] transformed = new double[numParts][];

		// if bbox is less than a pixel then decimate the geometry.  But orientation must
		// remain the same so geometry data must be parsed.
		if (bboxdecimate){
			coords=new double[1][];
			coords[0]=new double[2];
			transformed=new double[1][];
			transformed[0] = new double[2];
			coords[0][0]=buffer.getDouble();
			coords[0][1]=buffer.getDouble();
			try {
				mt.transform(coords[0], 0, transformed[0], 0, 1);
			} catch (Exception e) {
				ShapefileRenderer.LOGGER
						.severe("could not transform coordinates "
								+ e.getLocalizedMessage());
				transformed[0]=coords[0];
			}		
			}else{

            int partsInBBox=0;
			for (int part = 0; part < numParts; part++) {
				coords[part] = new double[2];
					coords[part][0] = buffer.getDouble();
					coords[part][1] = buffer.getDouble();
					
				if( !bbox.contains(coords[part][0], coords[part][1]) )
					continue;
				
				if (!mt.isIdentity()) {
					try {
						transformed[partsInBBox] = new double[2];
						mt.transform(coords[part], 0, transformed[partsInBBox], 0, 1);
					} catch (Exception e) {
						ShapefileRenderer.LOGGER
								.severe("could not transform coordinates "
										+ e.getLocalizedMessage());
						transformed[partsInBBox]=coords[part];
					}
				} else
				{
					transformed[partsInBBox] = new double[2];
					System.arraycopy(coords[part], 0, transformed[partsInBBox], 0, 1);
				}
				if( !screenMap.get((int)transformed[partsInBBox][0], (int)transformed[partsInBBox][1]))
					partsInBBox++;
			}
			if( partsInBBox==0 )
				return null;
			if( partsInBBox!=numParts ){
				double[][] tmp=new double[partsInBBox][];
				System.arraycopy(transformed, 0, tmp, 0, partsInBBox);
				transformed=tmp;
			}
		}
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
