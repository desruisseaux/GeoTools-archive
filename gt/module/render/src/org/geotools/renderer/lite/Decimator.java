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
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.util.LiteCoordinateSequence;
import org.geotools.util.LiteCoordinateSequenceFactory;

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
	public  Decimator(MathTransform screenToWorld){
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
	
	public final void decimateTransformGeneralize(Geometry geometry,MathTransform transform) throws TransformException 
	{
		if (geometry instanceof GeometryCollection) 
		{
			GeometryCollection collection = (GeometryCollection) geometry;
			for (int i = 0; i < collection.getNumGeometries(); i++) 
			{
				decimateTransformGeneralize(collection.getGeometryN(i),transform);
			}
		}
		else if (geometry instanceof Point) 
		{
			LiteCoordinateSequence seq = (LiteCoordinateSequence) ((Point) geometry).getCoordinateSequence();
			decimateTransformGeneralize(seq,transform);
		}
		else if (geometry instanceof Polygon) 
		{
			Polygon polygon = (Polygon) geometry;
			decimateTransformGeneralize(polygon.getExteriorRing(),transform);
			for (int i = 0; i < polygon.getNumInteriorRing(); i++) 
			{
				decimateTransformGeneralize(polygon.getInteriorRingN(i),transform);
			}
		}
		else if (geometry instanceof LineString) 
		{
			LiteCoordinateSequence seq = (LiteCoordinateSequence) ((LineString) geometry).getCoordinateSequence();
			decimateTransformGeneralize(seq,transform);
		}
	}
	
	/**
	 * decimates JTS geometries. 
	 */
	public final void decimate(Geometry geom) 
	{
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
	
	
	/**
	 *   1. remove any points that are within the spanx,spany.  We ALWAYS keep 1st and last point
	 *   2. transform to screen coordinates
	 *   3. remove any points that are close (span <1)
	 *   
	 * @param seq
	 * @param tranform
	 */
	private final void decimateTransformGeneralize(LiteCoordinateSequence seq,MathTransform transform) throws TransformException
	{
		  //decimates before XFORM
		  int ncoords = seq.size();
		  double  originalOrds[] = seq.getXYArray(); // 2*#of points
		  
		  if (ncoords <2)
		  {
			  if (ncoords ==1)  // 1 coordinate -- just xform it
			  {
				  double[] newCoordsXformed2= new double[2];
				  transform.transform(originalOrds, 0, newCoordsXformed2, 0, 1);
				  seq.setArray(newCoordsXformed2);
				  return;
			  }
			  else
				  return;  // ncoords =0
		  }
		  

		
		  
		 
		     // unfortunately, we have to keep things in double precion until after the transform or we could move things.
		  double[] allCoords = new double[ncoords*2]; // preallocate -- might not be full (throw away Z)
		    
		  allCoords[0] = originalOrds[0];  //allways have 1st one
		  allCoords[1] = originalOrds[1];
		  
		    int actualCoords = 1;
		    double lastX = allCoords[0];
		    double lastY = allCoords[1];
		    for (int t=1;t<(ncoords-1);t++)
		    {
		    	//see if this one should be added
		    	double  x =  originalOrds[t*2];
		    	double  y =   originalOrds[t*2+1];
		    	if ( (Math.abs(x-lastX )> spanx) ||  ( Math.abs(y-lastY )) >spany) 
		    	{
		    		allCoords[actualCoords*2] = x;
		    		allCoords[actualCoords*2+1] = y;
		    		lastX = x;
		    	    lastY = y;
		    		actualCoords++;   	
		    	}
		    }         
		    allCoords[actualCoords*2] =   originalOrds[(ncoords-1)*2]; //always have last one
		    allCoords[actualCoords*2+1] = originalOrds[(ncoords-1)*2+1];
		    actualCoords++;  
		    
		    double[] newCoordsXformed;
		    //DO THE XFORM
		    if ((transform == null) || (transform.isIdentity()) ) // no actual xform
    		{
		    	newCoordsXformed = allCoords;
    		}
		    else
		    {
		    	newCoordsXformed= new double[actualCoords*2];
				transform.transform(allCoords, 0, newCoordsXformed, 0, actualCoords);
		    }
			
			//GENERLIZE -- we should be in screen space so spanx=spany=1.0
			
			   // unfortunately, we have to keep things in double precion until after the transform or we could move things.
			   double[] finalCoords = new double[ncoords*2]; // preallocate -- might not be full (throw away Z)
			    
			   finalCoords[0] = newCoordsXformed[0];  //allways have 1st one
			   finalCoords[1] = newCoordsXformed[1];
			  
			    int actualCoordsGen = 1;
			    lastX = newCoordsXformed[0];
			    lastY = newCoordsXformed[1];
			    
			    for (int t=1;t<(actualCoords-1);t++)
			    {
			    	//see if this one should be added
			    	double  x =   newCoordsXformed[t*2];
			    	double  y =   newCoordsXformed[t*2+1];
			    	if ( (Math.abs(x-lastX )> 1) ||  ( Math.abs(y-lastY )) >1) 
			    	{
			    		finalCoords[actualCoordsGen*2] = x;
			    		finalCoords[actualCoordsGen*2+1] = y;
			    		lastX = x;
			    	    lastY = y;
			    	    actualCoordsGen++;   	
			    	}
			    }         
			    finalCoords[actualCoordsGen*2] =   newCoordsXformed[(actualCoords-1)*2]; //always have last one
			    finalCoords[actualCoordsGen*2+1] = newCoordsXformed[(actualCoords-1)*2+1];
			    actualCoordsGen++;  
			    
			    //stick back in
			    double[] seqDouble = new double[2*actualCoordsGen];
			    System.arraycopy(finalCoords,0,seqDouble, 0, actualCoordsGen*2);
				seq.setArray(seqDouble);
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
