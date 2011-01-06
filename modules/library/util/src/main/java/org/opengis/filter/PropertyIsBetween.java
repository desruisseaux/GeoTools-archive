/*$************************************************************************************************
 **
 ** $Id: PropertyIsBetween.java 1154 2007-12-19 22:29:42Z jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/filter/PropertyIsBetween.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.filter;

// OpenGIS direct dependencies
import org.opengis.annotation.XmlElement;
import org.opengis.filter.expression.Expression;


/**
 * A compact way of encoding a range check.
 * <p>
 * The lower and upper boundary values are inclusive.
 * </p>
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
@XmlElement("PropertyIsBetween")
public interface PropertyIsBetween extends Filter {
	/** Operator name used to check FilterCapabilities */
	public static String NAME = "Between";
	
    /**
     * Returns the expression to be compared by this operator.
     */
    @XmlElement("expression")
    Expression getExpression();

    /**
     * Returns the lower bounds (inclusive) an an expression.
     */
    @XmlElement("LowerBoundary")
    Expression getLowerBoundary();

    /**
     * Returns the upper bounds (inclusive) as an expression.
     */
    @XmlElement("UpperBoundary")
    Expression getUpperBoundary();

}