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
package org.geotools.gml3.bindings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;


/**
 * Utility class for gml3 encoding.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class GML3EncodingUtils {
    static DirectPosition[] positions(LineString line) {
        Coordinate[] coordinates = line.getCoordinates();
        DirectPosition[] dps = new DirectPosition[coordinates.length];

        for (int i = 0; i < dps.length; i++) {
            Coordinate coordinate = coordinates[i];
            dps[i] = new DirectPosition2D(coordinate.x, coordinate.y);
        }

        return dps;
    }

    static URI crs(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return null;
        }

        for (Iterator i = crs.getIdentifiers().iterator(); i.hasNext();) {
            Identifier id = (Identifier) i.next();

            try {
                return new URI("urn:x-ogc:def:crs:EPSG:6.11.2:" + id.getCode());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}
