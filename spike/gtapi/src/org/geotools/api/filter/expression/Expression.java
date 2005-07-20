/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/filter/expression/Expression.java,v $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.geotools.api.filter.expression;

import org.geotools.feature.Feature;

// OpenGIS direct dependencies


// Annotations
//import org.opengis.annotation.Extension;
//import org.opengis.annotation.XmlElement;


/**
 * Abstract super-interface for all the OGC Filter elements that compute values,
 * potentially using {@linkplain Feature feature} attributes in the computation.
 *
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
//@XmlElement("expression")
public interface Expression {
    /**
     * Evaluates the given expression based on the content of the given feature.
     */
    //@Extension
    Object evaluate(Feature feature);

    /**
     * Accepts a visitor. Subclasses must implement with a method whose content* is the following:
     * <pre>return visitor.{@linkplain ExpressionVisitor#visit visit}(this, extraData);</pre>
     */
    //@Extension
    Object accept(ExpressionVisitor visitor, Object extraData);
}
