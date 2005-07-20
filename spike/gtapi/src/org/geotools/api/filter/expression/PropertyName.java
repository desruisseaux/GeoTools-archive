/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/filter/expression/PropertyName.java,v $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.geotools.api.filter.expression;

// Annotations
//import org.opengis.annotation.XmlElement;


/**
 * Expression class whose value is computed by retrieving the value
 * of a {@linkplain org.opengis.feature.Feature feature}'s property.
 * <p>
 * Note: Note factory should take care to be ecplicit about the type returned:
 * <ul>
 * <li>SpatialExpression
 * <ul>MathExpression
 * </ul>
 * Factory can return an instnace of PropertyName that also is a MathExpression for example.
 * </p>
 * 
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
//@XmlElement("PropertyName")
public interface PropertyName extends Expression {
    /**
     * Returns the name of the property whose value will be returned by the
     * {@link #evaluate evaluate} method.
     */
    String getPropertyName();
}
