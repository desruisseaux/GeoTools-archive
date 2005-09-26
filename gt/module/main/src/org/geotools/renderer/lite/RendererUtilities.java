package org.geotools.renderer.lite;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

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
 *   Class for holding utility functions that are common tasks for people using the  "StreamingRenderer/Renderer".
 * 
 * 
 * @author dblasby
 */
public class RendererUtilities
{
   /**
    *   (Taken from the old LiteRenderer)
    * 
    * Sets up the affine transform
    * 
    * @param mapExtent the map extent
    * @param screenSize the screen size
    * @return a transform that maps from real world coordinates to the screen
    */
    public static AffineTransform worldToScreenTransform( Envelope mapExtent, Rectangle screenSize ) {
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();

        double tx = -mapExtent.getMinX() * scaleX ;
        double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight();
        
        AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY, tx, ty);
        AffineTransform originTranslation=AffineTransform.getTranslateInstance(screenSize.x, screenSize.y);
        originTranslation.concatenate(at);

        return originTranslation!=null?originTranslation:at;
    }
}