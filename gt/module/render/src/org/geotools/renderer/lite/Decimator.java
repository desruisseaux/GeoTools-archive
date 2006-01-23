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
package org.geotools.renderer.lite;

import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Accepts geometries and collapses all the vertices that will be rendered to the same pixel. 
 * @author jeichar
 * @since 2.1.x
 * @source $URL$
 */
public class Decimator {
	
	private double spanx=-1;
	private double spany=-1;

	/**
	 * @throws TransformException
	 * 
	 */
	public Decimator(MathTransform screenToWorld){
		if( screenToWorld != null ){
			double[] original=new double[]{0,0,1,1};
			double[] coords=new double[4];
			try {
				screenToWorld.transform(original,0,coords,0,2);
			} catch (TransformException e) {
				return;
			}
			this.spanx=Math.abs(coords[0]-coords[2]);
			this.spany=Math.abs(coords[1]-coords[3]);
		}else{
			this.spanx=1;
			this.spany=1;
		}
	}
	/**
	 * decimates JTS geometries. 
	 */
	public void decimate(Geometry geom) {
		if( spanx==-1 )
			return;
		if( geom instanceof MultiPoint ){
			// TODO check geometry and if its bbox is too small turn it into a 1 point geom
			return;
		}if( geom instanceof GeometryCollection ){
			// TODO check geometry and if its bbox is too small turn it into a 1-2 point geom
			// takes a bit of work because the geometry will need to be recreated.
				GeometryCollection collection=(GeometryCollection) geom;
				for (int i = 0; i < collection.getNumGeometries(); i++) {
					decimate(collection.getGeometryN(i));
				}
		}else if( geom instanceof LineString ){
			LineString line=(LineString) geom;
			LiteCoordinateSequence seq=(LiteCoordinateSequence)line.getCoordinateSequence();
			if( decimateOnEnvelope(line, seq) ){
				return;
			}			
			decimate(seq);
		}else if( geom instanceof Polygon ){
			Polygon line=(Polygon) geom;
			decimate( line.getExteriorRing() );
			for( int i=0; i<line.getNumInteriorRing(); i++){
				decimate(line.getInteriorRingN(i));
			}
		}
	}

	/**
	 * @param geom
	 * @param seq
	 * @return
	 */
	private boolean decimateOnEnvelope(Geometry geom, LiteCoordinateSequence seq) {
		Envelope env=geom.getEnvelopeInternal();
		if( env.getWidth()<=spanx && env.getHeight()<=spany ){
			double[] coords=seq.getArray();
			int dim=seq.getDimension();
			double[] newcoords = new double[dim*2];
			for(int i=0;i<dim;i++){
				newcoords[i]=coords[i];
				newcoords[dim+i]=coords[coords.length-dim+i];
			}
			seq.setArray(coords);
			return true;
		}
		return false;
	}
	
	private void decimate(LiteCoordinateSequence seq) {
		double[] coords=seq.getArray();
		int numDoubles=coords.length;
		int dim=seq.getDimension();
		int readDoubles=0;
		double prevx, currx, prevy,curry, diffx,diffy;
		for (int currentDoubles = 0; currentDoubles < coords.length; currentDoubles+=dim) {
			if( currentDoubles>=dim && currentDoubles<coords.length-1 ){
				prevx=coords[readDoubles-dim];
				currx=coords[currentDoubles];
				diffx=Math.abs(prevx-currx);
				prevy=coords[readDoubles-dim+1];
				curry=coords[currentDoubles+1];
				diffy=Math.abs(prevy-curry);
	        	if ( diffx>spanx || diffy>spany ){
	        		readDoubles = copyCoordinate(coords, dim, readDoubles, currentDoubles);	
	            }
			}else{
        		readDoubles = copyCoordinate(coords, dim, readDoubles, currentDoubles);				
			}
		}
		double[] newCoords=new double[readDoubles];
		System.arraycopy(coords,0,newCoords, 0, readDoubles);
		seq.setArray(newCoords);
	}
	/**
	 * @param coords
	 * @param dimension
	 * @param readDoubles
	 * @param currentDoubles
	 * @return
	 */
	private int copyCoordinate(double[] coords, int dimension, int readDoubles, int currentDoubles) {
		for( int i=0; i<dimension;i++){
			coords[readDoubles+i]=coords[currentDoubles+i];
		}
		readDoubles+=dimension;
		return readDoubles;
	}
}
