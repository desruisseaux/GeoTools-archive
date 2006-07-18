/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.renderer.lite;

import org.geotools.resources.CRSUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * DOCUMENT ME!
 *
 * @author simone
 */
public final class CRSTransformHashUtil {
    private int hash;
    private CoordinateReferenceSystem sourceCRS;
    private CoordinateReferenceSystem destinationCRS;

    public CRSTransformHashUtil(final CoordinateReferenceSystem sourceCRS,
        final CoordinateReferenceSystem destinationCRS) {
        this.sourceCRS = sourceCRS;
        this.destinationCRS = destinationCRS;
        this.hash = (3 * sourceCRS.hashCode())
            + (2 * destinationCRS.hashCode());
    }

    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (obj instanceof CRSTransformHashUtil) {
            final CRSTransformHashUtil obj1 = (CRSTransformHashUtil) obj;

            return CRSUtilities.equalsIgnoreMetadata(obj1.getSourceCRS(),
                sourceCRS)
            && CRSUtilities.equalsIgnoreMetadata(obj1.getDestinationCRS(),
                destinationCRS);
        }

        return false;
    }

    public int hashCode() {
        return hash;
    }

    public CoordinateReferenceSystem getDestinationCRS() {
        return destinationCRS;
    }

    public CoordinateReferenceSystem getSourceCRS() {
        return sourceCRS;
    }
}
