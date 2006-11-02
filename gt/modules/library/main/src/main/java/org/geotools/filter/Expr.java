/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.geotools.filter.FilterFactory;
import org.geotools.filter.expression.AddImpl;
import org.geotools.filter.expression.DivideImpl;
import org.geotools.filter.expression.MultiplyImpl;
import org.geotools.filter.expression.SubtractImpl;
import org.geotools.filter.identity.FeatureIdImpl;
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
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.spatialschema.geometry.Geometry;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This is FilterFactory for a very interesting implementation of Filter /
 * Expression.
 * <p>
 * Here is what Expr is all about:
 * <ul>
 * <li>method compatability with geoapi Filter / Expression interfaces
 * <li>true geoapi compatability as we transition to a common Feature and
 * Geometry model
 * <li>compatability with with 2.1 FeatureType and JTS Geometry
 * <li>compatability (or known converstion) to 2.1 Filter / Expression
 * interfaces
 * <li>chaining for ease of use
 * </ul>
 * 
 * <p>
 * This will make user of Java 5 type narrowing (so the FilterFactory methods
 * will return subtypes of Expr).
 * </p>
 * 
 * <p>
 * Note: FilterExpr is considered a normal Expr in which the result is known to
 * be<b>boolean</b>.
 * </p>
 * 
 * <p>
 * This method also implements the "chaining" concept you see in the collections
 * lib. This allows you to chain together a series of method calls to build up
 * your expression on a single line of java code.
 * </p>
 * 
 * <p>
 * From a modeling standpoint the chaining idea is incidental (it adds no
 * expressive power), it is simply a convience to users. It is also the
 * motivation for Expr being both an abstract class and Factory.
 * </p>
 * 
 * @author Jody Garnett
 */
public abstract class Expr implements FilterFactory {

	public FeatureId featureId(String id) {
		return new FeatureIdImpl( id );
	} 
	
	public And and(Filter f, Filter g ) {
		List/*<Filter>*/ list = new ArrayList/*<Filter>*/( 2 );
		list.add( f );
		list.add( g );
		return new AndImpl( this, list );
	}
	
	public And and(List/*<Filter>*/ filters) {
		return new AndImpl( this, filters );
	}
	
	public Or or(Filter f, Filter g) {
		List/*<Filter>*/ list = new ArrayList/*<Filter>*/( 2 );
		list.add( f );
		list.add( g );
		return new OrImpl( this, list );
	}

	public Or or(List/*<Filter>*/ filters) {
		return new OrImpl( this, filters );
	}
	
	/** Java 5 type narrowing used to advertise explicit implementation for chaining */
	public Not /*NotImpl*/ not(Filter filter) {
		return new NotImpl( this, filter );
	}

	
	public Id id( Set id ){
        return new FidFilterImpl( id );
    }
	
	public PropertyName property(String name) {
		return new AttributeExpressionImpl(name);
	}

	public PropertyIsBetween between(Expression expr, Expression lower,
			Expression upper) {
		return new IsBetweenImpl(this,lower,expr,upper);
	}

	public PropertyIsEqualTo equals(Expression expr1, Expression expr2) {
		return new IsEqualsToImpl(this,expr1,expr2);
	}

	public PropertyIsGreaterThan greater(Expression expr1, Expression expr2) {
		return new IsGreaterThanImpl(this,expr1,expr2);
	}

	public PropertyIsGreaterThanOrEqualTo greaterOrEqual(Expression expr1,
			Expression expr2) {
		return new IsGreaterThanOrEqualToImpl(this,expr1,expr2);
	}

	public PropertyIsLessThan less(Expression expr1, Expression expr2) {
		return new IsLessThenImpl(this,expr1,expr2);
	}

	public PropertyIsLessThanOrEqualTo lessOrEqual(Expression expr1,
			Expression expr2) {
		return new IsLessThenOrEqualToImpl(this,expr1,expr2);
	}

	public PropertyIsLike like(Expression expr, String pattern) {
		return like(expr,pattern,null,null,null);
	}

	public PropertyIsLike like(Expression expr, String pattern,
			String wildcard, String singleChar, String escape) {
		
		LikeFilterImpl filter = new LikeFilterImpl();
		filter.setExpression(expr);
		filter.setPattern(pattern,wildcard,singleChar,escape);
		
		return filter;
	}

	/**
	 * XXX Java 5 type narrowing used to make generated class explicit for chaining
	 */
	public PropertyIsNull /*IsNullImpl*/ isNull(Expression expr) {
		return new IsNullImpl( this, expr );
	}

	/**
	 * Checks if the bounding box of the feature's geometry overlaps the
	 * specified bounding box.
	 * <p>
	 * Similar to:
	 * <code>
	 * geom().disjoint( geom( bbox )).not()
	 * </code>
	 * </p>
	 */
	public BBOX bbox(String propertyName, double minx, double miny,
			double maxx, double maxy, String srs) {
		
		PropertyName name = property(propertyName);
		return bbox( name, minx, miny, maxx, maxy, srs );
	}

	public BBOX bbox(Expression e, double minx, double miny, double maxx, double maxy, String srs) {
		
		PropertyName name = null;
		if ( e instanceof PropertyName ) {
			name = (PropertyName) e;
		}
		else {
			throw new IllegalArgumentException();
		}
		
		BBoxExpression bbox = null;
		try {
			bbox = createBBoxExpression(new Envelope(minx,maxx,miny,maxy));
		} 
		catch (IllegalFilterException ife) {
			new IllegalArgumentException().initCause(ife);
		}
		
		BBOXImpl box = new BBOXImpl(this,e,bbox);
		box.setPropertyName( name.getPropertyName() );
		box.setSRS(srs);
		box.setMinX(minx);
		box.setMinY(miny);
		box.setMaxX(maxx);
		box.setMaxY(maxy);
		
		return box;
	}
	
	public Beyond beyond(String propertyName, Geometry geometry,
			double distance, String units) {
		
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return beyond( name, geom, distance, units );
	}

	public Beyond beyond( 
		Expression geometry1, Expression geometry2, double distance, String units
	) {
		
		BeyondImpl beyond = new BeyondImpl(this,geometry1,geometry2);
		beyond.setDistance(distance);
		beyond.setUnits(units);
		
		return beyond;
	}
	
	public Contains contains(String propertyName, Geometry geometry) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return contains( name, geom );
	}
	
	public Contains contains(Expression geometry1, Expression geometry2) {
		return new ContainsImpl( this, geometry1, geometry2 );
	}

	public Crosses crosses(String propertyName, Geometry geometry) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return crosses( name, geom );
	}
	
	public Crosses crosses(Expression geometry1, Expression geometry2) {
		return new CrossesImpl( this, geometry1, geometry2 );
	}
	

	public Disjoint disjoint(String propertyName, Geometry geometry) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return disjoint( name, geom );
	}

	
	public Disjoint disjoint(Expression geometry1, Expression geometry2) {
		return new DisjointImpl( this, geometry1, geometry2 );
	}
	
	public DWithin dwithin(String propertyName, Geometry geometry,
			double distance, String units) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return dwithin( name, geom, distance, units );
	}

	public DWithin dwithin(Expression geometry1, Expression geometry2, double distance, String units) {
		DWithinImpl dwithin =  new DWithinImpl( this, geometry1, geometry2 );
		dwithin.setDistance( distance );
		dwithin.setUnits( units );
		
		return dwithin;
	}
	
	public Equals equals(String propertyName, Geometry geometry) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return equal( name, geom );
	}
	
	public Equals equal(Expression geometry1, Expression geometry2) {
		return new EqualsImpl( this, geometry1, geometry2 );
	}

	public Intersects intersects(String propertyName, Geometry geometry) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return intersects( name, geom );
	}

	public Intersects intersects(Expression geometry1, Expression geometry2) {
		return new IntersectsImpl( this, geometry1, geometry2 );
	}
	
	public Overlaps overlaps(String propertyName, Geometry geometry) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return overlaps( name, geom );
	}

	public Overlaps overlaps(Expression geometry1, Expression geometry2) {
		return new OverlapsImpl( this, geometry1, geometry2 );
	}
	
	public Touches touches(String propertyName, Geometry geometry) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return touches( name, geom );
	}

	public Touches touches(Expression geometry1, Expression geometry2) {
		return new TouchesImpl(this,geometry1,geometry2);
	}
	
	public Within within(String propertyName, Geometry geometry) {
		PropertyName name = property(propertyName);
		Literal geom = literal(geometry);
		
		return within( name, geom );
	}

	public Within within(Expression geometry1, Expression geometry2) {
		return new WithinImpl( this, geometry1, geometry2 );
	}
	
	public Add add(Expression expr1, Expression expr2) {
		return new AddImpl(expr1,expr2);
	}

	public Divide divide(Expression expr1, Expression expr2) {
		return new DivideImpl(expr1,expr2);
	}

	public Multiply multiply(Expression expr1, Expression expr2) {
		return new MultiplyImpl(expr1,expr2);
	}

	public Subtract subtract(Expression expr1, Expression expr2) {
		return new SubtractImpl(expr1,expr2);
	}

	public Function function(String name, Expression[] args) {
		//TODO: Auto-generated method stub
		return null;
	}

	public Function function(String name, Expression arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Function function(String name, Expression arg1, Expression arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Function function(String name, Expression arg1, Expression arg2,
			Expression arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(Object obj) {
		try {
			return new LiteralExpressionImpl(obj);
		} 
		catch (IllegalFilterException e) {
			new IllegalArgumentException().initCause(e);
		}
		
		return null;
	}

	public Literal literal(byte b) {
		return new LiteralExpressionImpl(b);
	}

	public Literal literal(short s) {
		return new LiteralExpressionImpl(s);
	}

	public Literal literal(int i) {
		return new LiteralExpressionImpl(i);
	}

	public Literal literal(long l) {
		return new LiteralExpressionImpl(l);
	}

	public Literal literal(float f) {
		return new LiteralExpressionImpl(f);
	}

	public Literal literal(double d) {
		return new LiteralExpressionImpl(d);
	}

	public Literal literal(char c) {
		return new LiteralExpressionImpl(c);
	}

	public Literal literal(boolean b) {
		throw new UnsupportedOperationException("Filter api does not support boolean literals");
	}

}
