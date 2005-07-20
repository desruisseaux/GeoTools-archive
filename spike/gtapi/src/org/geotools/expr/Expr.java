package org.geotools.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.geotools.api.filter.And;
import org.geotools.api.filter.FeatureId;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.PropertyIsBetween;
import org.geotools.api.filter.PropertyIsEqualTo;
import org.geotools.api.filter.PropertyIsGreaterThan;
import org.geotools.api.filter.PropertyIsGreaterThanOrEqualTo;
import org.geotools.api.filter.PropertyIsLessThan;
import org.geotools.api.filter.PropertyIsLessThanOrEqualTo;
import org.geotools.api.filter.PropertyIsLike;
import org.geotools.api.filter.expression.Add;
import org.geotools.api.filter.expression.Divide;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.filter.expression.Function;
import org.geotools.api.filter.expression.Literal;
import org.geotools.api.filter.expression.Multiply;
import org.geotools.api.filter.expression.PropertyName;
import org.geotools.api.filter.expression.Subtract;
import org.geotools.api.filter.spatial.BBOX;
import org.geotools.api.filter.spatial.Beyond;
import org.geotools.api.filter.spatial.Contains;
import org.geotools.api.filter.spatial.Crosses;
import org.geotools.api.filter.spatial.DWithin;
import org.geotools.api.filter.spatial.Disjoint;
import org.geotools.api.filter.spatial.Equals;
import org.geotools.api.filter.spatial.Intersects;
import org.geotools.api.filter.spatial.Overlaps;
import org.geotools.api.filter.spatial.Touches;
import org.geotools.api.filter.spatial.Within;

import com.vividsolutions.jts.geom.Geometry;

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
public class Expr implements FilterFactory {

	public AndImpl and(Filter f, Filter g ) {
		List<Filter> list = new ArrayList<Filter>( 2 );
		list.add( f );
		list.add( g );
		return new AndImpl( this, list );
	}
	public And and(List<Filter> filters) {
		return new AndImpl( this, filters );
	}
	/** XXX: Java 5 varargs impl ... */
	public AndImpl and(Filter... filters) {
		return new AndImpl( this, Arrays.asList( filters ));
	}

	public OrImpl or(Filter f, Filter g) {
		List<Filter> list = new ArrayList<Filter>( 2 );
		list.add( f );
		list.add( g );
		return new OrImpl( this, list );
	}

	public OrImpl or(List<Filter> filters) {
		return new OrImpl( this, filters );
	}
	/** XXX: Java 5 varargs impl ... */
	public OrImpl or(Filter... filters) {
		return new OrImpl( this, Arrays.asList( filters ));
	}
	/** Java 5 type narrowing used to advertise explicit implementation for chaining */
	public NotImpl not(Filter filter) {
		return new NotImpl( this, filter );
	}

	public FeatureId featureId(Set ids) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public PropertyName property(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyIsBetween between(Expression expr, Expression lower,
			Expression upper) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyIsEqualTo equals(Expression expr1, Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyIsGreaterThan greater(Expression expr1, Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyIsGreaterThanOrEqualTo greaterOrEqual(Expression expr1,
			Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyIsLessThan less(Expression expr1, Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyIsLessThanOrEqualTo lessOrEqual(Expression expr1,
			Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyIsLike like(Expression expr, String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyIsLike like(Expression expr, String pattern,
			String wildcard, String singleChar, String escape) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * XXX Java 5 type narrowing used to make generated class explicit for chaining
	 */
	public IsNullImpl isNull(Expression expr) {
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
		// TODO Auto-generated method stub
		return null;
	}

	public Beyond beyond(String propertyName, Geometry geometry,
			double distance, String units) {
		// TODO Auto-generated method stub
		return null;
	}

	public Contains contains(String propertyName, Geometry geometry) {
		// TODO Auto-generated method stub
		return null;
	}

	public Crosses crosses(String propertyName, Geometry geometry) {
		// TODO Auto-generated method stub
		return null;
	}

	public Disjoint disjoint(String propertyName, Geometry geometry) {
		// TODO Auto-generated method stub
		return null;
	}

	public DWithin dwithin(String propertyName, Geometry geometry,
			double distance, String units) {
		// TODO Auto-generated method stub
		return null;
	}

	public Equals equals(String propertyName, Geometry geometry) {
		// TODO Auto-generated method stub
		return null;
	}

	public Intersects intersects(String propertyName, Geometry geometry) {
		// TODO Auto-generated method stub
		return null;
	}

	public Overlaps overlaps(String propertyName, Geometry geometry) {
		// TODO Auto-generated method stub
		return null;
	}

	public Touches touches(String propertyName, Geometry geometry) {
		// TODO Auto-generated method stub
		return null;
	}

	public Within within(String propertyName, Geometry geometry) {
		// TODO Auto-generated method stub
		return null;
	}

	public Add add(Expression expr1, Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Divide divide(Expression expr1, Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Multiply multiply(Expression expr1, Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Subtract subtract(Expression expr1, Expression expr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Function function(String name, Expression[] args) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(byte b) {
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(short s) {
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(long l) {
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(float f) {
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(double d) {
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(char c) {
		// TODO Auto-generated method stub
		return null;
	}

	public Literal literal(boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

}
