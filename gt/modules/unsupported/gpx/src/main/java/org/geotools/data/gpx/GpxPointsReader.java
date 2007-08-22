/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.gpx;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.data.FeatureReader;
import org.geotools.data.gpx.memory.Gpx;
import org.geotools.data.gpx.memory.GpxPoint;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;


public class GpxPointsReader implements FeatureReader {
    private final Iterator points;
    private final FeatureType featureType;
    private final Object[] attrs;
    private final GeometryFactory fac;

    GpxPointsReader(Gpx gpxData, FeatureType featureType) {
        ;
        points = gpxData.getWaypoints().iterator();
        this.featureType = featureType;
        attrs = new Object[featureType.getAttributeCount()];

        fac = new GeometryFactory();
    }

    public void close() throws IOException {
        // do nothing, data in memory;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public boolean hasNext() throws IOException {
        return points.hasNext();
    }

    public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        GpxPoint p = (GpxPoint) points.next();

        Coordinate coord = new Coordinate(p.getLon(), p.getLat(), p.getElevation());
        attrs[0] = fac.createPoint(coord); // the_geom
        attrs[1] = p.getName();
        attrs[2] = p.getDescription();
        attrs[3] = p.getComment();

        return featureType.create(attrs, p.getName());
    }
}
