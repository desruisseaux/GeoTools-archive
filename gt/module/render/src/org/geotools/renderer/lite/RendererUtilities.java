package org.geotools.renderer.lite;

import java.awt.Rectangle;
import java.awt.geom.*;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment
 * Committee (PMC) This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * version 2.1 of the License. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */

/**
 * Class for holding utility functions that are common tasks for people using the  "StreamingRenderer/Renderer".
 * 
 * 
 * @author dblasby
 * @source $URL$
 */
public class RendererUtilities
{
   /**
    * Sets up the affine transform <p/>
    * ((Taken from the old LiteRenderer))
    * 
    * @param mapExtent the map extent
    * @param paintArea the size of the rendering output area
    * @return a transform that maps from real world coordinates to the screen
    */
    public static AffineTransform worldToScreenTransform( Envelope mapExtent, Rectangle paintArea ) {
        double scaleX = paintArea.getWidth() / mapExtent.getWidth();
        double scaleY = paintArea.getHeight() / mapExtent.getHeight();

        double tx = -mapExtent.getMinX() * scaleX ;
        double ty = (mapExtent.getMinY() * scaleY) + paintArea.getHeight();
        
        AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY, tx, ty);
        AffineTransform originTranslation=AffineTransform.getTranslateInstance(paintArea.x, paintArea.y);
        originTranslation.concatenate(at);

        return originTranslation!=null?originTranslation:at;
    }
    
    /**
     * Creates the map's bounding box in real world coordinates <p/>
     * ((Taken from the old LiteRenderer))
     * @param worldToScreen a transform which converts World coordinates to
     *        screen pixel coordinates.
     * @param paintArea the size of the rendering output area
     */
    public static Envelope createMapEnvelope(Rectangle paintArea, AffineTransform worldToScreen)
            throws NoninvertibleTransformException{
        AffineTransform pixelToWorld = null;

        //Might throw NoninvertibleTransformException
        pixelToWorld = worldToScreen.createInverse();

        Point2D p1 = new Point2D.Double();
        Point2D p2 = new Point2D.Double();
        pixelToWorld.transform(new Point2D.Double(paintArea.getMinX(), paintArea.getMinY()), p1);
        pixelToWorld.transform(new Point2D.Double(paintArea.getMaxX(), paintArea.getMaxY()), p2);

        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        return new Envelope(
                Math.min(x1, x2), Math.max(x1, x2),
                Math.min(y1, y2), Math.max(y1, y2));        
    }

    /**
     * Find the scale denominator of the map.
     *   Method:
     *    1. find the diagonal distance (meters)
     *    2. find the diagonal distance (pixels)
     *    3. find the diagonal distance (meters) -- use DPI
     *    4. calculate scale (#1/#2)
     *
     *   NOTE: return the scale denominator not the actual scale (1/scale = denominator)
     *
     * TODO:  (SLD spec page 28):
     * Since it is common to integrate the output of multiple servers into a single displayed result in the
     * web-mapping environment, it is important that different map servers have consistent behaviour with respect to
     * processing scales, so that all of the independent servers will select or deselect rules at the same scales.
     * To insure consistent behaviour, scales relative to coordinate spaces must be handled consistently between map
     * servers. For geographic coordinate systems, which use angular units, the angular coverage of a map should be
     * converted to linear units for computation of scale by using the circumference of the Earth at the equator and
     * by assuming perfectly square linear units. For linear coordinate systems, the size of the coordinate space
     * should be used directly without compensating for distortions in it with respect to the shape of the real Earth.
     *
     * NOTE: we are actually doing a a much more exact calculation, and accounting for non-square pixels (which are allowed in WMS)
     *
     * @param envelope
     * @param coordinateReferenceSystem
     * @param imageWidth
     * @param imageHeight
     * @param DPI screen dots per inch (OGC standard is 90)
     * @return
     */
    public static double calculateScale(Envelope envelope, CoordinateReferenceSystem coordinateReferenceSystem,int imageWidth,int imageHeight,double DPI)
    throws Exception 
	{
    	//DJB: be much wiser if the requested image is larger than the world (this happens VERY OFTEN)
        // we first convert to WSG and check to see if we're outside the 'world' bbox
	       	double[] cs        = new double[4];
	    	double[] csLatLong = new double[4];
	    	Coordinate p1 = new Coordinate(envelope.getMinX(),envelope.getMinY());
	    	Coordinate p2 = new Coordinate(envelope.getMaxX(),envelope.getMaxY());
	    	cs[0] = p1.x;
	    	cs[1] = p1.y;
	    	cs[2] = p2.x;
	    	cs[3] = p2.y;    	 
	    	
	    	Hints hints=new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
	        CoordinateOperationFactory distanceOperationFactory = FactoryFinder.getCoordinateOperationFactory(hints);
	        
	         
	    	MathTransform transform = distanceOperationFactory.createOperation(coordinateReferenceSystem,DefaultGeographicCRS.WGS84).getMathTransform();
	    	transform.transform(cs, 0, csLatLong, 0, 2);
	    	
	    	//in long/lat format
	    	if  ( (csLatLong[0] <-180) || (csLatLong[0] >180) || (csLatLong[2] <-180) || (csLatLong[2] >180)
	    			|| (csLatLong[1] <-90) || (csLatLong[1] >90) || (csLatLong[3] <-90) || (csLatLong[3] >90)
			     )
	    	{
	    	      // we have a problem -- the bbox is outside the 'world' so distance will fail	
	    	      // we handle this by making a new measurement for a smaller portion of the image - the portion thats inside the world.
	    		  // if the request is outside the world then we need to throw an error
	    		
	    		if  ( (csLatLong[0] > csLatLong[2]) || (csLatLong[1] > csLatLong[3]) )
	    		  throw new Exception ("box is backwards");	
	    		if  ( ((csLatLong[0] <-180) || (csLatLong[0] >180)) && ((csLatLong[2] <-180) || (csLatLong[2] >180))
		    			&& ((csLatLong[1] <-90) || (csLatLong[1] >90)) && ((csLatLong[3] <-90) || (csLatLong[3] >90))
				     )
	    			throw new Exception ("world isnt in the requested bbox");
	    		//okay, all good.  We need to find the world bbox intersect the requested bbox
	    		// then we're going to convert that back to the original coordinate reference system
	    		// and from there we can find the (x1,y2) and (x2,y2) of this new bbox.
	    		// then we can do simple math to find the distance.
	    		
	    		double[] newCsLatLong = new double[4]; // intersected with the world bbox
	    		
	    		newCsLatLong[0] = Math.min(Math.max(csLatLong[0],-180),180) ;
	    		newCsLatLong[1] = Math.min(Math.max(csLatLong[1],-90),90) ;
	    		newCsLatLong[2] = Math.min(Math.max(csLatLong[2],-180),180) ;
	    		newCsLatLong[3] = Math.min(Math.max(csLatLong[3],-90),90) ;
	    		
	    		MathTransform transform2 = distanceOperationFactory.createOperation(DefaultGeographicCRS.WGS84,coordinateReferenceSystem).getMathTransform();
	    		double[] origProject        = new double[4];
		    	transform.transform(newCsLatLong, 0, origProject, 0, 2);
		    	
		    	//have the truncated bbox in the original projection, so we can find the image (x,y) for the two points.
		    	
		    	double image_min_x = (origProject[0] - envelope.getMinX() )/envelope.getWidth() *imageWidth;
		    	double image_max_x = (origProject[2] - envelope.getMinX() )/envelope.getWidth() *imageWidth;
		    	
		    	double image_min_y = (origProject[1] - envelope.getMinY() )/envelope.getHeight() *imageHeight;
		    	double image_max_y = (origProject[3] - envelope.getMinY() )/envelope.getHeight() *imageHeight;
		    	
		    	double distance_ground = JTS.orthodromicDistance(
		    			         new Coordinate(newCsLatLong[0],newCsLatLong[1] ),
		    			         new Coordinate(newCsLatLong[2],newCsLatLong[3] ),
		    			         DefaultGeographicCRS.WGS84
		    			                        );
		    	double pixel_distance =  Math.sqrt( (image_max_x-image_min_x) *(image_max_x-image_min_x) + (image_max_y-image_min_y)*(image_max_y-image_min_y));
		    	double pixel_distance_m = pixel_distance/ DPI * 2.54 / 100.0;
		    	return distance_ground/ pixel_distance_m; // remember, this is the denominator, not the actual scale;
	    	}

    	
    	
	        double diagonalGroundDistance = JTS.orthodromicDistance(
	               p1,p2,	          
	                coordinateReferenceSystem
	                );
	        // pythagorus theorm
	        double diagonalPixelDistancePixels = Math.sqrt( imageWidth*imageWidth+imageHeight*imageHeight);
	        double diagonalPixelDistanceMeters = diagonalPixelDistancePixels / DPI * 2.54 / 100; // 2.54 = cm/inch, 100= cm/m
	        return diagonalGroundDistance/diagonalPixelDistanceMeters; // remember, this is the denominator, not the actual scale;
    	}
    	
        
     
}
