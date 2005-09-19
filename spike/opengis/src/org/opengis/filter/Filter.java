package org.opengis.filter;

import org.opengis.feature.Feature;

/**
 * A filter is used to define a set (often to to be operated upon).
 * <p>
 * The operating set can be comprised of one or more enumerated features or a set
 * of features defined by specifying spatial and non-spatial constraints on the
 * geometric and scalar properties of a feature type.
 * <p>
 * Roughly speaking, a filter encodes the information present in the {@code WHERE}
 * clause of a SQL statement.  There are various subclasses of this class that
 * implement many types of filters, such as simple property comparisons or spatial
 * queries.
 * </p>
 * Depending on the implementation Filter is defined to work against:
 * <ul>
 * <li>Attribute (including ComplexAttribute, Feature and FeatureCollection)
 * <li>Object
 * <li>Metadata (such as Citation)
 * </ul>
 * <p>
 * XXX: You may also consider Filter as a "BooleanExpression" where true indicates set
 * membership.
 * </p>
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
public interface Filter {
	/**
     * Given an instance, this method determines whether test(s)represented by this filter object are passed.
     * 
     * XXX: This is a Java 5 type narrowing of Expression
     * @see accepts
     */ //@Extension
    Boolean evaluate(Object instance);

    /**
     * Helper method for Java 1.4 geotools users.
     * <p>
     * XXX: Added accepts as a convience method for those forward porting from
     * geotools 2.1, and to prevent the need for Java 1.4 users to cast.
     * <p>
     * This is an implementation of: <code>
     * <b>return</b> Boolean.True.equals( evaulate( feature ) ); 
     * </code>
     */
    boolean accepts( Feature feature );
}
