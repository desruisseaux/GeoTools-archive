/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/filter/spatial/BinarySpatialOperator.java,v $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.geotools.api.filter.spatial;

// Annotations
//import org.opengis.annotation.XmlElement;


/**
 * Abstract superclass for filter operators that perform some sort of spatial
 * comparison on two geometric objects.
 *
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
//@XmlElement("BinarySpatialOpType")
public interface BinarySpatialOperator extends SpatialOperator {
    /**
     * Returns an expression that will be evaluated to determine the first
     * operand to the spatial predicate represented by this operator.  The
     * result of evaluating this expression must be a geometry object.
     */
    //@XmlElement("expression")
    SpatialExpression getExpression1();

    /**
     * Returns an expression that will be evaluated to determine the second
     * operand to the spatial predicate represented by this operator.  The
     * result of evaluating this expression must be a geometry object.
     * <p>
     */
    //@XmlElement("expression")
    SpatialExpression getExpression2();
}
