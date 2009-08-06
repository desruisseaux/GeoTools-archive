/*
 *    OSGeom -- Geometry Collab
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2001-2009 Department of Geography, University of Bonn
 *    (C) 2001-2009 lat/lon GmbH
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
package org.osgeo.geometry.multi;

import org.osgeo.commons.uom.Measure;
import org.osgeo.commons.uom.Unit;
import org.osgeo.geometry.primitive.Curve;

/**
 * Specialized aggregation type for {@link Curve} objects.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface MultiCurve extends MultiGeometry<Curve> {

    /**
     * The length of a <code>MultiCurve</code> is calculated as the sum of the length of all contained
     * {@link Curve}s.
     * 
     * @param requestedUnit
     * @return accumulated length
     */
    public Measure getLength( Unit requestedUnit );
}
