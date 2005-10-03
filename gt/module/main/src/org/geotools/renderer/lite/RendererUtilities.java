package org.geotools.renderer.lite;

import java.awt.Rectangle;
import java.awt.geom.*;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
    public static double calculateScale(Envelope envelope, CoordinateReferenceSystem coordinateReferenceSystem,int imageWidth,int imageHeight,double DPI) throws Exception {
        double diagonalGroundDistance = CRS.distance(
                new Coordinate(envelope.getMinX(),envelope.getMinY()),
                new Coordinate(envelope.getMaxX(),envelope.getMaxY()),
                coordinateReferenceSystem
                );
        // pythagorus theorm
        double diagonalPixelDistancePixels = Math.sqrt( imageWidth*imageWidth+imageHeight*imageHeight);
        double diagonalPixelDistanceMeters = diagonalPixelDistancePixels / DPI * 2.54 / 100; // 2.54 = cm/inch, 100= cm/m
        
        
        return diagonalGroundDistance/diagonalPixelDistanceMeters; // remember, this is the denominator, not the actual scale;
    }
}