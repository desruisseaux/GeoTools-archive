/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source: /cvsroot/geoapi/src/org/opengis/filter/Filter.java,v $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.geotools.api.filter;

import org.geotools.api.filter.expression.Expression;
import org.geotools.feature.Feature;

// OpenGIS direct dependencies

// Annotations
//import org.opengis.annotation.Extension;
//import org.opengis.annotation.XmlElement;


/**
 * The abstract base class for filters. A filter is used to define a set of
 * {@linkplain Feature feature} instances that are to be operated upon. The
 * operating set can be comprised of one or more enumerated features or a set
 * of features defined by specifying spatial and non-spatial constraints on the
 * geometric and scalar properties of a feature type.
 * <p>
 * Roughly speaking, a filter encodes the information present in the {@code WHERE}
 * clause of a SQL statement.  There are various subclasses of this class that
 * implement many types of filters, such as simple property comparisons or spatial
 * queries.
 * 
 * XXX Consider this the same as BooleanExpression :-)
 * 
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
//@XmlElement("Filter")
public interface Filter extends Expression {
    /**
     * Given a feature, this method determines whether the feature passes the
     * test(s) represented by this filter object.
     * 
     * XXX This is a Java 5 type narrowing of Expression
     * @see accepts
     */
    //@Extension
    Boolean evaluate(Feature feature);

    /**
     * XXX: Added accepts as a convience method for those forward porting from
     * geotools 2.1, and to prevent the need for Java 1.4 users to cast.
     * <p>
     * This is an implementation of: <code>
     * <b>return</b> Boolean.True.equals( evaulate( feature ) ); 
     * </code>
     */
    boolean accepts( Feature feature );
}
