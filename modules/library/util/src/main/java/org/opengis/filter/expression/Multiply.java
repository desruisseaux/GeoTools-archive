/*$************************************************************************************************
 **
 ** $Id: Multiply.java 1154 2007-12-19 22:29:42Z jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/filter/expression/Multiply.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.filter.expression;

// Annotations
import org.opengis.annotation.XmlElement;


/**
 * Encodes the operation of multiplication.
 * <p>
 * Instances of this interface implement their {@link #evaluate evaluate} method by
 * computing the numeric product of their {@linkplain #getExpression1 first} and
 * {@linkplain #getExpression2 second} operand.
 * </p>
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
@XmlElement("Mul")
public interface Multiply extends BinaryExpression {
	/** Operator name used to check FilterCapabilities */
	public static String NAME = "Mul";
}
