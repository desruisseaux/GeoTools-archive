/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/filter/BinaryComparisonOperator.java,v $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.geotools.api.filter;

import org.geotools.api.filter.expression.Expression;

// Annotations
//import org.opengis.annotation.XmlElement;


/**
 * Filter that compare exactly two values against each
 * other.  The nature of the comparison is dependent on the subclass.
 *
 * XXX Origional described as: "Abstract base class"
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
//@XmlElement("BinaryComparisonOpType")
public interface BinaryComparisonOperator extends Filter {
    /**
     * Returns the first of the two expressions to be compared by this operator.
     */
    //@XmlElement("expression")
    Expression getExpression1();

    /**
     * Returns the second of the two expressions to be compared by this operator.
     */
    //@XmlElement("expression")
    Expression getExpression2();
}
