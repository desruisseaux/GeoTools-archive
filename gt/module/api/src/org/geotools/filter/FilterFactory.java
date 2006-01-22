/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.factory.Factory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.opengis.feature.FeatureTypeFactory;


/**
 * This specifies the interface to create filters.
 *
 * @version $Id$
 *
 * @task TODO: This needs to be massively overhauled.  This should be the
 *       source of immutability of filters.  See {@link FeatureTypeFactory},
 *       as that provides a good example of what this should look like.  The
 *       mutable factory to create immutable objects is a good model for this.
 *       The creation methods should only create fully formed filters.  This
 *       in turn means that all the set functions in the filters should be
 *       eliminated.  When rewriting this class/package, keep in mind
 *       FilterSAXParser in the filter module, as the factory should fit
 *       cleanly with that, and should handle sax parsing without too much
 *       memory overhead.
 * @task REVISIT: resolve errors, should all throw errors?
 */
public interface FilterFactory extends Factory {
    /**
     * Creates a logic filter from two filters and a type.
     *
     * @param filter1 the first filter to join.
     * @param filter2 the second filter to join.
     * @param filterType must be a logic type.
     *
     * @return the newly constructed logic filter.
     *
     * @throws IllegalFilterException If there were any problems creating the
     *         filter, including wrong type.
     */
    public LogicFilter createLogicFilter(Filter filter1, Filter filter2,
        short filterType) throws IllegalFilterException;

    /**
     * Creates an empty logic filter from a type.
     *
     * @param filterType must be a logic type.
     *
     * @return the newly constructed logic filter.
     *
     * @throws IllegalFilterException If there were any problems creating the
     *         filter, including wrong type.
     */
    public LogicFilter createLogicFilter(short filterType)
        throws IllegalFilterException;

    /**
     * Creates a logic filter with an initial filter..
     *
     * @param filter the initial filter to set.
     * @param filterType Must be a logic type.
     *
     * @return the newly constructed logic filter.
     *
     * @throws IllegalFilterException If there were any problems creating the
     *         filter, including wrong type.
     */
    public LogicFilter createLogicFilter(Filter filter, short filterType)
        throws IllegalFilterException;

    /**
     * Creates a BBox Expression from an envelope.
     *
     * @param env the envelope to use for this bounding box.
     *
     * @return The newly created BBoxExpression.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public BBoxExpression createBBoxExpression(Envelope env)
        throws IllegalFilterException;

    /**
     * Creates an Integer Literal Expression.
     *
     * @param i the int to serve as literal.
     *
     * @return The new Literal Expression
     */
    public LiteralExpression createLiteralExpression(int i);

    /**
     * Creates a Math Expression
     *
     * @return The new Math Expression
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public MathExpression createMathExpression() throws IllegalFilterException;

    /**
     * Creates a new Fid Filter with no initial fids.
     *
     * @return The new Fid Filter.
     */
    public FidFilter createFidFilter();

    /**
     * Creates an AttributeExpression using the supplied xpath.
     * 
     * <p>
     * The supplied xpath can be used to query a varity of content - most
     * notably Features.
     * </p>
     *
     * @param xpath XPath used to retrive value
     *
     * @return The new Attribtue Expression
     */
    public AttributeExpression createAttributeExpression(String xpath);

    /**
     * Creates a Attribute Expression given a schema and attribute path.
     * 
     * <p>
     * If you supply a schema, it will be used as a sanitch check for the
     * provided path.
     * </p>
     *
     * @param schema the schema to get the attribute from, or null
     * @param xpath the xPath of the attribute to compare.
     *
     * @return The new Attribute Expression.
     *
     * @throws IllegalFilterException if there were creation problems.
     *
     * @deprecated use createAttributeExpression( xpath ), will be removed for
     *             GeoTools 2.3
     */
    public AttributeExpression createAttributeExpression(FeatureType schema,
        String xpath) throws IllegalFilterException;

    /**
     * Shortcut the process - will only grab values matching AttributeType.
     *
     * @param at
     *
     * @return The new Attribtue Expression
     *
     * @throws IllegalFilterException if there were creation problems
     *
     * @deprecated use createAttributeExpression( at ), will be removed for
     *             GeoTools 2.3
     */
    public AttributeExpression createAttributeExpression(AttributeType at)
        throws IllegalFilterException;

    /**
     * Creates a Literal Expression from an Object.
     *
     * @param o the object to serve as the literal.
     *
     * @return The new Literal Expression
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public LiteralExpression createLiteralExpression(Object o)
        throws IllegalFilterException;

    /**
     * Creates a new compare filter of the given type.
     *
     * @param type the type of comparison - must be a compare type.
     *
     * @return The new compare filter.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public CompareFilter createCompareFilter(short type)
        throws IllegalFilterException;

    /**
     * Creates an empty Literal Expression
     *
     * @return The new Literal Expression.
     */
    public LiteralExpression createLiteralExpression();

    /**
     * Creates a String Literal Expression
     *
     * @param s the string to serve as the literal.
     *
     * @return The new Literal Expression
     */
    public LiteralExpression createLiteralExpression(String s);

    /**
     * Creates a Double Literal Expression
     *
     * @param d the double to serve as the literal.
     *
     * @return The new Literal Expression
     */
    public LiteralExpression createLiteralExpression(double d);

    /**
     * Creates a Attribute Expression with an initial schema.
     *
     * @param schema the schema to create with.
     *
     * @return The new Attribute Expression.
     * @deprecated use {@link #createAttributeExpression(String)} instead.
     */
    public AttributeExpression createAttributeExpression(FeatureType schema);

    /**
     * Creates a Math Expression of the given type.
     *
     * @param expressionType must be a math expression type.
     *
     * @return The new Math Expression.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public MathExpression createMathExpression(short expressionType)
        throws IllegalFilterException;

    /**
     * Creates an empty Null Filter.
     *
     * @return The new Null Filter.
     */
    public NullFilter createNullFilter();

    /**
     * Creates an empty Between Filter.
     *
     * @return The new Between Filter.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public BetweenFilter createBetweenFilter() throws IllegalFilterException;

    /**
     * Creates a Geometry Filter.
     *
     * @param filterType the type to create, must be a geometry type.
     *
     * @return The new Geometry Filter.
     *
     * @throws IllegalFilterException if the filterType is not a geometry.
     */
    public GeometryFilter createGeometryFilter(short filterType)
        throws IllegalFilterException;

    /**
     * Creates a Geometry Distance Filter
     *
     * @param filterType the type to create, must be beyond or dwithin.
     *
     * @return The new  Expression
     *
     * @throws IllegalFilterException if the filterType is not a geometry
     *         distance type.
     */
    public GeometryDistanceFilter createGeometryDistanceFilter(short filterType)
        throws IllegalFilterException;

    /**
     * Creates a Fid Filter with an initial fid.
     *
     * @param fid the feature ID to create with.
     *
     * @return The new FidFilter.
     */
    public FidFilter createFidFilter(String fid);

    /**
     * Creates a Like Filter.
     *
     * @return The new Like Filter.
     */
    public LikeFilter createLikeFilter();

    /**
     * Creates a Function Expression.
     *
     * @param name the function name.
     *
     * @return The new Function Expression.
     */
    public FunctionExpression createFunctionExpression(String name);

    /**
     * Creates an Environment Variable
     *
     * @param name the function name.
     *
     * @return The new Function Expression.
     */
    public EnvironmentVariable createEnvironmentVariable(String name);
}
