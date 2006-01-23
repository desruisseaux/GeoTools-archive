/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 * FilterFactoryImpl.java
 *
 * Created on 24 October 2002, 16:16
 */
package org.geotools.filter;

import java.util.Map;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Implementation of the FilterFactory, generates the filter implementations in
 * defaultcore.
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class FilterFactoryImpl implements FilterFactory {
    /**
     * Creates a new instance of FilterFactoryImpl
     */
    public FilterFactoryImpl() {
    }

    /**
     * Creates an AttributeExpression using the supplied xpath.
     * <p>
     * The supplied xpath can be used to query a varity of
     * content - most notably Features.
     * </p>
     * @return The new Attribtue Expression
     */
    public AttributeExpression createAttributeExpression( String xpath){
    	return new AttributeExpressionImpl( xpath );
    }
    /**
     * Creates a Attribute Expression with an initial schema.
     *
     * @param schema the schema to create with.
     *
     * @return The new Attribute Expression.
     */
    public AttributeExpression createAttributeExpression(FeatureType schema) {
        return new AttributeExpressionImpl(schema);
    }

    /**
     * Creates a Attribute Expression given a schema and attribute path.
     *
     * @param schema the schema to get the attribute from.
     * @param path the xPath of the attribute to compare.
     *
     * @return The new Attribute Expression.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public AttributeExpression createAttributeExpression(FeatureType schema,
            String path) throws IllegalFilterException {
            return new AttributeExpressionImpl(schema, path);
        }
    public AttributeExpression createAttributeExpression(AttributeType at) throws IllegalFilterException {
            return new AttributeExpressionImpl2(at);
        }

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
        throws IllegalFilterException {
        return new BBoxExpressionImpl(env);
    }

    /**
     * Creates an empty Between Filter.
     *
     * @return The new Between Filter.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public BetweenFilter createBetweenFilter() throws IllegalFilterException {
        return new BetweenFilterImpl();
    }

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
        throws IllegalFilterException {
        return new CompareFilterImpl(type);
    }

    /**
     * Creates a new Fid Filter with no initial fids.
     *
     * @return The new Fid Filter.
     */
    public FidFilter createFidFilter() {
        return new FidFilterImpl();
    }

    /**
     * Creates a Fid Filter with an initial fid.
     *
     * @param fid the feature ID to create with.
     *
     * @return The new FidFilter.
     */
    public FidFilter createFidFilter(String fid) {
        return new FidFilterImpl(fid);
    }

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
        throws IllegalFilterException {
        return new GeometryFilterImpl(filterType);
    }

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
        throws IllegalFilterException {
        return new CartesianDistanceFilter(filterType);
    }

    /**
     * Creates a Like Filter.
     *
     * @return The new Like Filter.
     */
    public LikeFilter createLikeFilter() {
        return new LikeFilterImpl();
    }

    /**
     * Creates an empty Literal Expression
     *
     * @return The new Literal Expression.
     */
    public LiteralExpression createLiteralExpression() {
        return new LiteralExpressionImpl();
    }

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
        throws IllegalFilterException {
        return new LiteralExpressionImpl(o);
    }

    /**
     * Creates an Integer Literal Expression.
     *
     * @param i the int to serve as literal.
     *
     * @return The new Literal Expression
     */
    public LiteralExpression createLiteralExpression(int i) {
        return new LiteralExpressionImpl(i);
    }

    /**
     * Creates a Double Literal Expression
     *
     * @param d the double to serve as the literal.
     *
     * @return The new Literal Expression
     */
    public LiteralExpression createLiteralExpression(double d) {
        return new LiteralExpressionImpl(d);
    }

    /**
     * Creates a String Literal Expression
     *
     * @param s the string to serve as the literal.
     *
     * @return The new Literal Expression
     */
    public LiteralExpression createLiteralExpression(String s) {
        return new LiteralExpressionImpl(s);
    }

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
        throws IllegalFilterException {
        return new LogicFilterImpl(filterType);
    }

    /**
     * Creates a logic filter with an initial filter.
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
        throws IllegalFilterException {
        return new LogicFilterImpl(filter, filterType);
    }

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
        short filterType) throws IllegalFilterException {
        return new LogicFilterImpl(filter1, filter2, filterType);
    }

    /**
     * Creates a Math Expression
     *
     * @return The new Math Expression
     */
    public MathExpression createMathExpression() {
        return new MathExpressionImpl();
    }

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
        throws IllegalFilterException {
        return new MathExpressionImpl(expressionType);
    }

    /**
     * Creates a Function Expression.
     *
     * @param name the function name.
     *
     * @return The new Function Expression.
     */
    public FunctionExpression createFunctionExpression(String name) {
        int index = -1;

        if ((index = name.indexOf("Function")) != -1) {
            name = name.substring(0, index);
        }

        name = name.toLowerCase().trim();

        char c = name.charAt(0);
        name = name.replaceFirst("" + c, "" + Character.toUpperCase(c));

        try {
            //TODO: Replace the following return statement with something that uses 
            //FactoryFinder to find the function.
            java.util.Iterator it = org.geotools.factory.FactoryFinder.factories(FunctionExpression.class);
            String funName = "";
            FunctionExpression exp = null;
            while ((funName != "found") && (it.hasNext())){
                FunctionExpression fe = (FunctionExpression) it.next();
                funName = fe.getName();
                if (funName.equalsIgnoreCase(name)){
                    exp = fe;
                    funName = "found";
                }
            }
            return exp;
            
      
        } catch (Exception e) {
            throw new RuntimeException("Unable to create class " + name
                + "Function", e);  
        }
    }

    /**
     * Creates an empty Null Filter.
     *
     * @return The new Null Filter.
     */
    public NullFilter createNullFilter() {
        return new NullFilterImpl();
    }
    
    public EnvironmentVariable createEnvironmentVariable(String name){
        if(name.equalsIgnoreCase("MapScaleDenominator")){
            return new MapScaleDenominatorImpl();
        }
         throw new RuntimeException("Unknown environment variable:" + name);
    }

	public Map getImplementationHints() {
		// TODO Auto-generated method stub
		return null;
	}
}
