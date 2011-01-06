/*$************************************************************************************************
 **
 ** $Id: Boundary.java 1356 2009-02-20 10:02:26Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/geometry/Boundary.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.geometry;

import static org.opengis.annotation.Obligation.MANDATORY;
import static org.opengis.annotation.Specification.ISO_19107;

import org.opengis.annotation.UML;
import org.opengis.geometry.complex.Complex;


/**
 * The abstract root data type for all the data types used to represent the boundary of geometric
 * objects. Any subclass of {@link Geometry} will use a subclass of {@code Boundary} to
 * represent its boundary through the operation {@link Geometry#getBoundary}. By the nature of
 * geometry, boundary objects are cycles.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as">ISO 19107</A>
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 1.0
 */
@UML(identifier="GM_Boundary", specification=ISO_19107)
public interface Boundary extends Complex {
    /**
     * Always returns {@code true} since boundary objects are cycles.
     *
     * @return Always {@code true}.
     */
    @UML(identifier="isCycle", obligation=MANDATORY, specification=ISO_19107)
    boolean isCycle();
}