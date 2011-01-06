/*$************************************************************************************************
 **
 ** $Id: BinarySpatialOperator.java 1154 2007-12-19 22:29:42Z jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/filter/spatial/BinarySpatialOperator.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.filter.spatial;

// OpenGIS direct dependencies
import org.opengis.filter.expression.Expression;

// Annotations
import org.opengis.annotation.XmlElement;


/**
 * Abstract superclass for filter operators that perform some sort of spatial
 * comparison on two geometric objects.
 *
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
@XmlElement("BinarySpatialOpType")
public interface BinarySpatialOperator extends SpatialOperator {
	/**
     * Returns an expression that will be evaluated to determine the first
     * operand to the spatial predicate represented by this operator.  The
     * result of evaluating this expression must be a geometry object.
     */
    @XmlElement("expression")
    Expression getExpression1();

    /**
     * Returns an expression that will be evaluated to determine the second
     * operand to the spatial predicate represented by this operator.  The
     * result of evaluating this expression must be a geometry object.
     */
    @XmlElement("expression")
    Expression getExpression2();
}
