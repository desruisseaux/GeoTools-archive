/*$************************************************************************************************
 **
 ** $Id: ValueSegment.java 1263 2008-07-09 17:25:51Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/coverage/ValueSegment.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.coverage;

import java.util.Set;
import org.opengis.geometry.primitive.Curve;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Limits of a value segment specified by two values of the arc-length parameter of the
 * {@linkplain Curve curve} underlying its parent {@linkplain ValueCurve value curve}.
 *
 * @version ISO 19123:2004
 * @author  Alessio Fabiani
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 2.1
 */
@UML(identifier="CV_ValueSegment", specification=ISO_19123)
public interface ValueSegment {
    /**
     * Returns the value of the arc-length parameter of the parent
     * curve at the start of this value segment.
     *
     * @return The value at the start of this segment.
     */
    @UML(identifier="startParameter", obligation=MANDATORY, specification=ISO_19123)
    double getStartParameter();

    /**
     * Returns the value of the arc-length parameter of the parent
     * curve at the end of this value segment.
     *
     * @return The value at the end of this segment.
     */
    @UML(identifier="endParameter", obligation=MANDATORY, specification=ISO_19123)
    double getEndParameter();

    /**
     * Returns the set of <var>point</var>-<var>value</var> pairs that provide control
     * values for the interpolation. Linear interpolation requires a minimum of two control
     * values, usually those at the beginning and end of the value segment. Additional
     * control values are required to support interpolation by higher order functions.
     *
     * @return The set of control points and values.
     */
    @UML(identifier="controlPoint", obligation=MANDATORY, specification=ISO_19123)
    Set<PointValuePair> getControlPoints();
}