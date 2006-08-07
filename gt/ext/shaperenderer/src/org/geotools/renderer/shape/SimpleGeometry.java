/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.renderer.shape;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.shapefile.shp.ShapeType;


/**
 *
 * @source $URL$
 */
public class SimpleGeometry {
    public final ShapeType type;
    public final double[][] coords;
    public final Envelope bbox;

    public SimpleGeometry(ShapeType shapeType, double[][] coords, Envelope bbox) {
        this.type = shapeType;
        this.coords = coords;
        this.bbox = bbox;
    }

    public String toString() {
        return coords[0].toString();
    }
}
