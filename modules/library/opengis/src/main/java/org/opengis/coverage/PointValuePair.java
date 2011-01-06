/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2005 Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.coverage;

import org.opengis.geometry.primitive.Point;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * A {@linkplain GeometryValuePair geometry-value pair} that has a {@linkplain Point point}
 * as the value of its geometry attribute.
 *
 * @version ISO 19123:2004
 * @author  Wim Koolhoven
 * @since   GeoAPI 2.1
 */
@UML(identifier="CV_PointValuePair", specification=ISO_19123)
public interface PointValuePair extends GeometryValuePair {
    /**
     * The point that is a member of this <var>point</var>-<var>value</var> pair.
     */
    @UML(identifier="geometry", obligation=MANDATORY, specification=ISO_19123)
    DomainObject<Point> getGeometry();
}
