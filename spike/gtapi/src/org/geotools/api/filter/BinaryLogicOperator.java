/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/filter/BinaryLogicOperator.java,v $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.geotools.api.filter;

// J2SE direct dependencies
import java.util.List;

// Annotations
//import org.opengis.annotation.XmlElement;


/**
 * Filter for logical operators that accept two, or more, other logical values
 * as inputs.
 * <p>
 * Currently, the only two subclasses are {@link And} and {@link Or}.
 * </p>
 * 
 * XXX Origional called this "Abstract super-interface"
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
//@XmlElement("BinaryLogicOpType")
public interface BinaryLogicOperator extends Filter {
    /**
     * Returns a list containing all of the child filters of this object.
     * <p>
     * This list will contain at least two elements, and each element will be an
     * instance of {@code Filter}.  Implementations of this interface are
     * encouraged to return either a copy of their internal list or an
     * immutable wrapper around their internal list.  This is because this
     * specification requires {@code Filter} objects to be immutable.
     * </p>
     * <p>
     * XXX: I don't see the need for at least two elements, infact the return value
     * should be defined for zero and 1 elements to help with stability
     * </p>
     */
    List<Filter> getChildren();
}
