/*$************************************************************************************************
 **
 ** $Id: MultiPointImpl.java,v 1.2 2005/04/01 23:22:46 cdillard Exp $
 **
 ** $Source: /cvs/ctree/LiteGO1/src/jar/com/polexis/lite/spatialschema/geometry/aggregate/MultiPointImpl.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotools.geometry.jts.spatialschema.geometry.aggregate;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.aggregate.MultiPoint;

/**
 */
public class MultiPointImpl extends AggregateImpl 
	implements MultiPoint {

    public MultiPointImpl() {
        this(null);
    }

    public MultiPointImpl(final CoordinateReferenceSystem crs) {
        super(crs);
    }
}
