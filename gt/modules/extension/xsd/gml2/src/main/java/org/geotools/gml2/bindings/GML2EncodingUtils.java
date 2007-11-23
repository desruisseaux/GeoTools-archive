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
package org.geotools.gml2.bindings;

import java.util.Iterator;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Utility methods used by gml2 bindigns when encodding.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class GML2EncodingUtils {
    static final int LON_LAT = 0;
    static final int LAT_LON = 1;
    static final int INAPPLICABLE = 2;

    public static String epsgCode(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return null;
        }

        for (Iterator i = crs.getIdentifiers().iterator(); i.hasNext();) {
            Identifier id = (Identifier) i.next();

            //return "EPSG:" + id.getCode();
            if ((id.getAuthority() != null)
                    && id.getAuthority().getTitle().equals(Citations.EPSG.getTitle())) {
                return id.getCode();
            }
        }

        return null;
    }

    public static String crs(CoordinateReferenceSystem crs) {
        String code = epsgCode(crs);
        int axisOrder = axisOrder(crs);

        if (code != null) {
            if ((axisOrder == LON_LAT) || (axisOrder == INAPPLICABLE)) {
                return "http://www.opengis.net/gml/srs/epsg.xml#" + code;
            } else {
                //return "urn:x-ogc:def:crs:EPSG:6.11.2:" + code;
                return "urn:x-ogc:def:crs:EPSG:" + code;
            }
        }

        return null;
    }

    /**
     * Returns the axis order of the provided {@link CoordinateReferenceSystem} object.
     * @param crs
     * @return <ul>
     *         <li>LON_LAT if the axis order is longitude/latitude</li>
     *         <li>LAT_LON if the axis order is latitude/longitude</li>
     *         <li>INAPPLICABLE if the CRS does not deal with longitude/latitude
     *         (such as vertical or engineering CRS)</li>
     */
    static int axisOrder(CoordinateReferenceSystem crs) {
        CoordinateSystem cs = null;

        if (crs instanceof ProjectedCRS) {
            ProjectedCRS pcrs = (ProjectedCRS) crs;
            cs = pcrs.getBaseCRS().getCoordinateSystem();
        } else if (crs instanceof GeographicCRS) {
            cs = crs.getCoordinateSystem();
        } else {
            return INAPPLICABLE;
        }

        int dimension = cs.getDimension();
        int longitudeDim = -1;
        int latitudeDim = -1;

        for (int i = 0; i < dimension; i++) {
            AxisDirection dir = cs.getAxis(i).getDirection().absolute();

            if (dir.equals(AxisDirection.EAST)) {
                longitudeDim = i;
            }

            if (dir.equals(AxisDirection.NORTH)) {
                latitudeDim = i;
            }
        }

        if ((longitudeDim >= 0) && (latitudeDim >= 0)) {
            if (longitudeDim < latitudeDim) {
                return LON_LAT;
            } else {
                return LAT_LON;
            }
        }

        return INAPPLICABLE;
    }
}
