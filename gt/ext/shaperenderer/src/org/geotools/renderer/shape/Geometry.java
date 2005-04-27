/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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

import com.vividsolutions.jts.geom.Envelope;

class Geometry {
    
    
    int type;
    private double[][] coords;
    private Envelope bbox;
    
    public Geometry( int shapeType, double[][] coords, Envelope bbox ) {
        this.type=shapeType;
        this.coords=coords;
        this.bbox=bbox;
    }
    
    
}