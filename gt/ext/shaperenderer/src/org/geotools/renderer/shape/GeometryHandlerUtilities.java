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

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;

import org.geotools.data.shapefile.shp.ShapeType;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Useful methods common to all geometry handlers
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class GeometryHandlerUtilities {

	/**
	 * @param buffer
	 * @return
	 */
	public static Envelope readBounds(ByteBuffer buffer) {
		double[] tmpbbox = new double[4];
		tmpbbox[0] = buffer.getDouble();
		tmpbbox[1] = buffer.getDouble();
		tmpbbox[2] = buffer.getDouble();
		tmpbbox[3] = buffer.getDouble();
	
		Envelope geomBBox = new Envelope(tmpbbox[0], tmpbbox[2], tmpbbox[1],
				tmpbbox[3]);
		return geomBBox;
	}
	public static void transform(ShapeType type, MathTransform mt, double[] src, double[] dest ) throws TransformException{
		
		boolean startPointTransformed=true;
		for( int i=0; i<dest.length; i+=2){
			try{
				mt.transform(src, i, dest, i, 1);
				if ( !startPointTransformed ){
					startPointTransformed=true;
					for (int j = 0; j < i; j+=2) {
						dest[j]=src[i];
						dest[j+1]=src[i+1];
					}
				}
			}catch (TransformException e) {
				if( i==0 ){
					startPointTransformed=false;
				} else
				if( startPointTransformed ){
					if( i==dest.length-2 &&
							(type==ShapeType.POLYGON ||
									type==ShapeType.POLYGONZ ||
									type==ShapeType.POLYGONM )){
						dest[i]=dest[0];
						dest[i+1]=dest[1];
					}else{
						dest[i]=dest[i-2];
						dest[i+1]=dest[i-1];
					}
				}
			}
		}
		if( !startPointTransformed ){
			throw new TransformException("Unable to transform any of the points in the shape");
		}
	}
	public static Point2D calculateSpan(MathTransform mt) throws NoninvertibleTransformException, TransformException {
		MathTransform screenToWorld = mt.inverse();
		double[] original = new double[] { 0, 0, 1, 1 };
		double[] coords = new double[4];
		screenToWorld.transform(original, 0, coords, 0, 2);
		Point2D span=new Point2D.Double(Math.abs(coords[0] - coords[2]),
				Math.abs(coords[1] - coords[3]));
		return span;
	}
	public static ScreenMap calculateScreenSize(Envelope env, MathTransform mt, boolean hasOpacity) throws TransformException, NoninvertibleTransformException {
		if (hasOpacity)
			// if opacity then this short optimization cannot be used
			// so return a screenMap that always says to write there.
			return new ScreenMap(0, 0){
			public boolean get(int x, int y) {
				return false; 
			}
			public void set(int x, int y, boolean value) {
				return;
			}
		};
		double[] worldSize=new double[]{
				env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY()
		};
		double[] screenSize=new double[4];
		mt.transform(worldSize, 0, screenSize, 0, 2);
		int width=Math.abs((int) (screenSize[1]-screenSize[0]));
		int height=Math.abs((int) (screenSize[3]-screenSize[2]));
		return new ScreenMap(width+1,height+1);
		
	
	}
	
}
