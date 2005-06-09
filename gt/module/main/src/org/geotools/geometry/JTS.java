/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.geometry;

// OpenGIS dependencies
import java.util.NoSuchElementException;

import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.geometry.jts.PreciseCoordinateSequenceTransformer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * JTS Geometry utility methods, bringing geotools to JTS.
 * <p>
 * Offers geotools based services such as reprojection.
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>transformation
 * <li>coordinate sequence editing
 * <li>common coordinate sequence implementations for specific uses
 * </ul>
 * </p>
 * @author jgarnett
 * @since 0.6.0
 */
public class JTS {
    
    
    public static Envelope empty(){
        Envelope envelope = new Envelope();
        envelope.setToNull();        
        return envelope;
    }
    
    public static class ReferencedEnvelope extends Envelope{
        private CoordinateReferenceSystem crs = null;
        public ReferencedEnvelope(Envelope env, CoordinateReferenceSystem crs){
            super(env);
            this.crs = crs;
        }
        
        public CoordinateReferenceSystem getCRS(){
            return crs;
        }
    }
    
    public static ReferencedEnvelope create(Envelope env, CoordinateReferenceSystem crs){
        return new ReferencedEnvelope(env,crs);
    }

    /**
     * Transforms the Envelope using the MathTransform.
     * The envelope is densified (extra points put around the outside edge) to provide 
     * a 'better' new envelope for high deformed situations.
     * 
     * 
     * @param envelope the envelope to transform
     * @param transform the transformation to use
     * @param npoints decification of each side of the rectange.
     * @return a new Envelope
     * @throws TransformException 
     */
    public static Envelope transform(Envelope envelope, MathTransform transform,int npoints) 
    throws TransformException 
	{          
        npoints +=2;// for the corners.
        
        double[] coordsEnvPoly = new double[ (4*npoints)*2];
        double[] newCoords = new double[ (4*npoints)*2];
        
        int offset=0;
        for (int t=0;t<npoints;t++)
        {
        		//left side
        	coordsEnvPoly[offset]  = envelope.getMinX();
        	coordsEnvPoly[offset+1]  = envelope.getMinY() + (envelope.getMaxY()-envelope.getMinY() )/(npoints-1)*(t);
 
        		//right side
          	coordsEnvPoly[offset+2]  = envelope.getMaxX();
        	coordsEnvPoly[offset+3]  = envelope.getMinY() + (envelope.getMaxY()-envelope.getMinY() )/(npoints-1)*(t);

        	offset+=4;
        }
        
        for (int t=0;t<npoints;t++)
        {
        		//bottom side
         	coordsEnvPoly[offset]  = envelope.getMinX() + (envelope.getMaxX()-envelope.getMinX() )/(npoints-1)*(t);
          	coordsEnvPoly[offset+1]  = envelope.getMinY();
          	 
      		//top side
         	coordsEnvPoly[offset+2]  = envelope.getMinX() + (envelope.getMaxX()-envelope.getMinX() )/(npoints-1)*(t);
          	coordsEnvPoly[offset+3]  = envelope.getMaxY();
 
        	offset+=4;
        }
        
        xform(transform, coordsEnvPoly, newCoords, 2);
//        transform.transform(coordsEnvPoly, 0, newCoords, 0, npoints*4);
        
        // now find the min/max of the result
        Envelope result = new Envelope();
   
        for (int t=0;t<npoints*4;t++)
        {
        	result.expandToInclude( newCoords[t*2],newCoords[t*2+1]);
        }
        
        return result;
    }

    /**
     * Like a transform but eXtreme!
     * 
     * Transforms an array of coordinate using the provided math transform.  
     * Each Coordinate is transformed seperately. In case of a transform exception then the new value
     * of the coordinate is the last coordinate correctly transformed.
     *
     * @param mt
     * @param src
     * @param dest
     * @throws TransformException
     */
    public static void xform(MathTransform mt, double[] src, double[] dest, int dimensions) throws TransformException{
        xform(mt, src, dest, dimensions, .1f);
    }
    /**
     * Like a transform but eXtreme!
     * 
     * Transforms an array of coordinate using the provided math transform.  
     * Each Coordinate is transformed seperately. In case of a transform exception then the new value
     * of the coordinate is the last coordinate correctly transformed.
     *
     * @param mt
     * @param src
     * @param dest
     * @throws TransformException
     */
    public static void xform(MathTransform mt, double[] src, double[] dest, int dimensions, 
            float failureThreshold ) throws TransformException{
        int numCoords=dest.length/2;
        int failures=0;
        int threshold=(int) ((float)numCoords*failureThreshold);
        boolean startPointTransformed=true;
        for( int i=0; i<dest.length; i+=dimensions){
            try{
                mt.transform(src, i, dest, i, 1);
                if ( !startPointTransformed ){
                    startPointTransformed=true;
                    for (int j = 0; j < i; j++) {
                        dest[j]=src[i-dimensions];
                    }
                }
            }catch (TransformException e) {
                failures++;
                if( i==0 ){
                    startPointTransformed=false;
                } else
                if( startPointTransformed ){
                    for( int j = i-dimensions; j < dimensions; j++ ) {
                        dest[j]=src[i-dimensions];                        
                    }
                }
            }
        }
        if( !startPointTransformed ){
            throw new TransformException("Unable to transform any of the points in the shape");
        }
    }
    
    
    /**
     * Transforms the Envelope using the MathTransform.
     * @param envelope the envelope to transform
     * @param transform the transformation to use
     * @return a new Envelope
     * @throws TransformException 
     */
    public static Envelope transform(Envelope envelope, MathTransform transform) throws TransformException {
        double[] coords=new double[]{envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY()};
        double[] newcoords=new double[4];
        transform.transform(coords, 0, newcoords, 0, 2);
        return new Envelope(newcoords[0],newcoords[2],newcoords[1],newcoords[3]);
    }

    public static ReferencedEnvelope transform(ReferencedEnvelope envelope, CoordinateReferenceSystem crs, boolean lenient) throws TransformException, OperationNotFoundException, NoSuchElementException, FactoryException {
        double[] coords=new double[]{envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY()};
        double[] newcoords=new double[4];
        
        MathTransform transform = CRS.transform(envelope.getCRS(),crs,lenient);
        transform.transform(coords, 0, newcoords, 0, 2);
        return new ReferencedEnvelope(new Envelope(newcoords[0],newcoords[2],newcoords[1],newcoords[3]),crs);
    }
    
    /**
     * Creates a GeometryCoordinateSequenceTransformer.  This basic transformer
     * transforms the vertices and assumes that the underlying data structure is
     * an array of Coordinate objects.
     * 
     * @return a GeometryCoordinateSequenceTransformer
     */
    public static GeometryCoordinateSequenceTransformer createGeometryTransformer(){
    	return new GeometryCoordinateSequenceTransformer();
    }
    
    /**
     * Creates a GeometryCoordinateSequenceTransformer.  This transformer
     * is more accurate than the basic geometry transformer.  It assumes that the underlying data structure is
     * an array of Coordinate objects.
     * @param flatness The error in the transform is linked to the "flattening", the higher the flattening,
     * the bigger the error, but also, the lesser the number of points that will be used
     * to represent the resulting coordinate sequence.
     * @return a GeometryCoordinateSequenceTransformer
     */
    public static GeometryCoordinateSequenceTransformer createPreciseGeometryTransformer(double flatness){
        PreciseCoordinateSequenceTransformer t=new PreciseCoordinateSequenceTransformer();
        t.setFlatness(flatness);
    	return new GeometryCoordinateSequenceTransformer(t);
    }
   
    /**
     * Transforms the geometry using the default transformer.
     * 
     * @param geom The geom to transform
     * @param transform the transform to use during the transformation.
     * @return the transformed geometry.  It will be a new geometry.
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    public static Geometry transform( Geometry geom, MathTransform transform ) throws MismatchedDimensionException, TransformException{
    	GeometryCoordinateSequenceTransformer transformer=createGeometryTransformer();
    	transformer.setMathTransform(transform);
		return transformer.transform(geom);
	}
    
    /**
     * Transforms the geometry using the Precise transformer.
     * 
     * @param geom The geom to transform
     * @param flatness the "flatness" of the new geometry.  Higher is more accurate but has more vertices.
     * @param transform the transform to use during the transformation.
     * @return the transformed geometry.  It will be a new geometry.
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
	public static Geometry preciseTransform( Geometry geom, double flatness, MathTransform transform ) throws MismatchedDimensionException, TransformException{
    	GeometryCoordinateSequenceTransformer transformer=createPreciseGeometryTransformer(flatness);
    	transformer.setMathTransform(transform);
		return transformer.transform(geom);	    
	}
    
    /**
     * 
     * Transforms the coordinate using the provided math transform.
     * @param source the source coordinate that will be transformed
     * @param dest the coordinate that will be set.  May be null or the source coordinate (or new coordinate of course). 
     * return the destination coordinate if not null or a new Coordinate.
     * @throws TransformException 
     */     
    public static Coordinate transform( Coordinate source, Coordinate dest, MathTransform transform ) throws TransformException{
        if( dest==null)
            dest=new Coordinate();
        
        double[] array=null;
        if ( transform.getSourceDimensions()==2 )
            array=new double[]{ source.x, source.y };
        else if( transform.getSourceDimensions()==3 )
            array=new double[]{ source.x, source.y, source.z };
        
        transform.transform(array,0,array,0,1);
        
        dest.x=array[0];
        dest.y=array[1];
        if ( transform.getTargetDimensions()==3)
            dest.z=array[2];
        
        return dest;
    }
    
    /**
     * Transforms the envelope from its current crs to WGS84 coordinate system.
     *
     * @param env The envelope to transform.
     * @param crs The CRS the envelope is currently in.
     * @return The envelope transformed to be in WGS84 CRS.
     * @throws OperationNotFoundException
     * @throws NoSuchElementException
     * @throws FactoryException
     * @throws TransformException
     *
     * @todo We may catch some exceptions and rethrown them in a simplier one.
     *       After all, this is a convenience method (not an "official" one).
     */
    public static Envelope toGeographic(Envelope env, CoordinateReferenceSystem crs)
            throws OperationNotFoundException, NoSuchElementException, FactoryException, TransformException
    {
    	if (crs.equals(DefaultGeographicCRS.WGS84)) {
            return env;
        }
        MathTransform transform = FactoryFinder.getCoordinateOperationFactory(null)
                .createOperation(crs, DefaultGeographicCRS.WGS84).getMathTransform();
        return JTS.transform(env, transform);
    }
}
