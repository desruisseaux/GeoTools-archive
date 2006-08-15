/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 *
 * Created on 24 October 2002, 16:16
 */
package org.geotools.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.filter.expression.AddImpl;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.expression.BBoxExpression;
import org.geotools.filter.expression.DivideImpl;
import org.geotools.filter.expression.ExpressionType;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.LiteralExpression;
import org.geotools.filter.expression.MathExpression;
import org.geotools.filter.expression.MultiplyImpl;
import org.geotools.filter.expression.SubtractImpl;
import org.geotools.filter.spatial.BBOXImpl;
import org.geotools.filter.spatial.BeyondImpl;
import org.geotools.filter.spatial.ContainsImpl;
import org.geotools.filter.spatial.CrossesImpl;
import org.geotools.filter.spatial.DWithinImpl;
import org.geotools.filter.spatial.DisjointImpl;
import org.geotools.filter.spatial.EqualsImpl;
import org.geotools.filter.spatial.IntersectsImpl;
import org.geotools.filter.spatial.OverlapsImpl;
import org.geotools.filter.spatial.TouchesImpl;
import org.geotools.filter.spatial.WithinImpl;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Implementation of the FilterFactory, generates the filter implementations in
 * defaultcore.
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class FilterFactoryImpl extends Expr implements FilterFactory {
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
     * 
     * @deprecated @see org.geotools.filter.FilterFactory#createCompareFilter(short)
     */
    public CompareFilter createCompareFilter(short type)
        throws IllegalFilterException {
    	
    	switch(type) {
    		case FilterType.COMPARE_EQUALS:
    			return new IsEqualsToImpl(this);
    			
    		case FilterType.COMPARE_NOT_EQUALS:
    			return new IsNotEqualToImpl(this);
    			
    		case FilterType.COMPARE_GREATER_THAN:
    			return new IsGreaterThanImpl(this);
    			
    		case FilterType.COMPARE_GREATER_THAN_EQUAL:
    			return new IsGreaterThanOrEqualToImpl(this);
    			
    		case FilterType.COMPARE_LESS_THAN:
    			return new IsLessThenImpl(this);
    			
    		case FilterType.COMPARE_LESS_THAN_EQUAL:
    			return new IsLessThenOrEqualToImpl(this);
    			
    		case FilterType.BETWEEN:
    			return new BetweenFilterImpl(this);
    	}
    	
    	throw new IllegalFilterException("Must be one of <,<=,==,>,>=,<>");
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
    	switch(filterType) {
    		case FilterType.GEOMETRY_EQUALS:
    			return new EqualsImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_DISJOINT:
    			return new DisjointImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_DWITHIN:
    			return new DWithinImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_INTERSECTS:
    			return new IntersectsImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_CROSSES:
    			return new CrossesImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_WITHIN:
    			return new WithinImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_CONTAINS:
    			return new ContainsImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_OVERLAPS:
    			return new OverlapsImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_BEYOND:
    			return new BeyondImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_BBOX:
    			return new BBOXImpl(this,null,null);
    			
    		case FilterType.GEOMETRY_TOUCHES:
    			return new TouchesImpl(this,null,null);
    	}
       
        throw new IllegalFilterException("Not one of the accepted spatial filter types.");
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
    	
    	switch(filterType) {
			case FilterType.GEOMETRY_BEYOND:
				return new BeyondImpl(this,null,null);
				
			case FilterType.GEOMETRY_DWITHIN:
				return new DWithinImpl(this,null,null);
			
    	}
   
    	throw new IllegalFilterException("Not one of the accepted spatial filter types.");
        
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
     *         
     * @deprecated use one of {@link org.opengis.filter.FilterFactory#and(Filter, Filter)}
     * 	{@link org.opengis.filter.FilterFactory#or(Filter, Filter)}
     * 	{@link org.opengis.filter.FilterFactory#not(Filter)}
     */
    public LogicFilter createLogicFilter(short filterType)
        throws IllegalFilterException {
    	
    	List children = new ArrayList();
    	switch (filterType) {
	    	case FilterType.LOGIC_AND:
	    		return new AndImpl(this,children);
	    	case FilterType.LOGIC_OR:
	    		return new OrImpl(this,children);
	    	case FilterType.LOGIC_NOT:
	    		return new NotImpl(this);
    	}
    	
        throw new IllegalFilterException("Must be one of AND,OR,NOT.");
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
     *         
     * @deprecated use one of {@link org.opengis.filter.FilterFactory#and(Filter, Filter)}
     * 	{@link org.opengis.filter.FilterFactory#or(Filter, Filter)}
     * 	{@link org.opengis.filter.FilterFactory#not(Filter)}
     */
    public LogicFilter createLogicFilter(Filter filter, short filterType)
        throws IllegalFilterException {
    	
    	List children = new ArrayList();
    	children.add(filter);
    	
    	switch (filterType) {
	    	case FilterType.LOGIC_AND:
	    		return new AndImpl(this,children);
	    	case FilterType.LOGIC_OR:
	    		return new OrImpl(this,children);
	    	case FilterType.LOGIC_NOT:
	    		return new NotImpl(this,filter);
    	}
    	
        throw new IllegalFilterException("Must be one of AND,OR,NOT.");
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
     *         
     * @deprecated use one of {@link org.opengis.filter.FilterFactory#and(Filter, Filter)}
     * 	{@link org.opengis.filter.FilterFactory#or(Filter, Filter)}
     * 	{@link org.opengis.filter.FilterFactory#not(Filter)}
     */
    public LogicFilter createLogicFilter(Filter filter1, Filter filter2,
        short filterType) throws IllegalFilterException {
    	
    	List children = new ArrayList();
    	children.add(filter1);
    	children.add(filter2);
    	
    	switch (filterType) {
	    	case FilterType.LOGIC_AND:
	    		return new AndImpl(this,children);
	    	case FilterType.LOGIC_OR:
	    		return new OrImpl(this,children);
	    	case FilterType.LOGIC_NOT:
	    		//TODO: perhaps throw an exception here?
	    		return new NotImpl(this,filter1);
    	}
    	
        throw new IllegalFilterException("Must be one of AND,OR,NOT.");
    }
    
    

    /**
     * Creates a Math Expression
     *
     * @return The new Math Expression
     * 
     * @deprecated use one of
     * 	{@link org.opengis.filter.FilterFactory#add(Expression, Expression)}
     * 	{@link org.opengis.filter.FilterFactory#subtract(Expression, Expression)}
     * 	{@link org.opengis.filter.FilterFactory#multiply(Expression, Expression)}
     * 	{@link org.opengis.filter.FilterFactory#divide(Expression, Expression)}
     * 
     */
    public MathExpression createMathExpression() {
    	throw new UnsupportedOperationException();
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
    	
    	switch(expressionType) {
	    	case ExpressionType.MATH_ADD:
	    		return new AddImpl(null,null);
	    	case ExpressionType.MATH_SUBTRACT:
	    		return new SubtractImpl(null,null);
	    	case ExpressionType.MATH_MULTIPLY:
	    		return new MultiplyImpl(null,null);
	    	case ExpressionType.MATH_DIVIDE:
	    		return new DivideImpl(null,null);
    	}	
    	
        throw new IllegalFilterException("Unsupported math expression");
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
	
	public Filter and(Filter f1, Filter f2) {
		org.opengis.filter.Filter f = and((org.opengis.filter.Filter)f1,(org.opengis.filter.Filter)f2);
		return (Filter)f;
	}
	
	public Filter or(Filter f1, Filter f2) {
		org.opengis.filter.Filter f = or((org.opengis.filter.Filter)f1,(org.opengis.filter.Filter)f2);
		return (Filter)f;
	}
	
	public Filter not(Filter f) {
		org.opengis.filter.Filter f1 = not((org.opengis.filter.Filter)f);
		return (Filter)f1;
	}
	
	public SortBy sort(String propertyName, SortOrder order) {
		throw new UnsupportedOperationException();
	}
}
