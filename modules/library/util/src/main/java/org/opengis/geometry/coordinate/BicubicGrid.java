/*$************************************************************************************************
 **
 ** $Id: BicubicGrid.java 1352 2009-02-18 20:46:17Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/geometry/coordinate/BicubicGrid.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.geometry.coordinate;

import static org.opengis.annotation.Obligation.MANDATORY;
import static org.opengis.annotation.Specification.ISO_19107;

import java.util.List;

import org.opengis.annotation.UML;


/**
 * A {@linkplain GriddedSurface gridded surface} that uses cubic polynomial splines as the
 * horizontal and vertical curves. The initial tangents for the splines are often replaced
 * by an extra pair of rows (and columns) of control points.
 * <p>
 * The horizontal and vertical curves require initial and final tangent vectors for a complete
 * definition. These values are supplied by the four methods defined in this interface.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as">ISO 19107</A>
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 2.0
 */
@UML(identifier="GM_BicubicGrid", specification=ISO_19107)
public interface BicubicGrid extends GriddedSurface {
    /**
     * Returns the initial tangent vectors.
     */
    @UML(identifier="horiVectorAtStart", obligation=MANDATORY, specification=ISO_19107)
    List<double[]> getHorizontalVectorAtStart();

    /**
     * Returns the initial tangent vectors.
     */
    @UML(identifier="horiVectorAtEnd", obligation=MANDATORY, specification=ISO_19107)
    List<double[]> getHorizontalVectorAtEnd();

    /**
     * Returns the initial tangent vectors.
     */
    @UML(identifier="vertVectorAtStart", obligation=MANDATORY, specification=ISO_19107)
    List<double[]> getVerticalVectorAtStart();

    /**
     * Returns the initial tangent vectors.
     */
    @UML(identifier="vertVectorAtEnd", obligation=MANDATORY, specification=ISO_19107)
    List<double[]> getVerticalVectorAtEnd();
}