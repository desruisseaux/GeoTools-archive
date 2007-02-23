/*$************************************************************************************************
 **
 ** $Id: SurfaceBoundaryImpl.java,v 1.7 2005/11/02 05:39:33 crossley Exp $
 **
 ** $Source: /cvs/ctree/LiteGO1/src/jar/com/polexis/lite/spatialschema/geometry/primitive/SurfaceBoundaryImpl.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotools.geometry.jts.spatialschema.geometry.primitive;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.primitive.Ring;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;

/**
 * LiteGO1 implementation of the SurfaceBoundary interface.
 */
public class SurfaceBoundaryImpl extends PrimitiveBoundaryImpl implements SurfaceBoundary {
    private Ring exterior;
    private Ring [] interior;

    public SurfaceBoundaryImpl(CoordinateReferenceSystem crs, Ring exterior, Ring [] interior) {
        super(crs);
        this.exterior = exterior;
        this.interior = interior;
    }

    /**
     * Returns the exterior ring, or {@code null} if none.
     */
    public Ring getExterior() {
        return exterior;
    }

    /**
     * Returns the interior rings.
     */
    public Ring [] getInteriors() {
        return interior;
    }
}
