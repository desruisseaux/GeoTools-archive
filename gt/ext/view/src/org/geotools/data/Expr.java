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
package org.geotools.data;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Filter/Expression construction kit.
 * <p>
 * I can't take Filter anymore, the turning point was the fact that I
 * needed a FeatureType to make a AttributeExpression. Simply *no*,
 * I cannot expect customer code to have to jump through hoops so that
 * my impmentation is easier. Late binding is required.
 * </p>
 * <p>
 * The answer - while not completely forcing DataStores away from
 * Expression/Filter is to make a class that captures all the same
 * information and can do the late binding.
 * </p>
 * <p>
 * On the way we thre we are not going to have the difference between
 * Filter/Expression. Expr can make an Expression, and Expr can make a
 * Filter.
 * <p>
 * @author Jody Garnett
 */
public class Expr {
	protected FilterFactory factory = FilterFactory.createFilterFactory();
	
	/** Filter.NONE */
	Filter filter(FeatureType schema) {
		return Filter.NONE;
	}	
	/** Boolean.TRUE */
	Expression expression( FeatureType schema ) {
		try {
			return factory.createLiteralExpression( Boolean.TRUE );
		} catch (IllegalFilterException e) {
			return null;
		} 
	}
	Expr fid( final String featureID ){
		return new FidsExpr( featureID );		
	}
	Expr fid( final Set fids ){
		return new FidsExpr( fids );
	}
	Expr not(){
		return new NotExpr( this );
	}
	Expr and( Expr expr ){
		return new AndExpr( this, expr );		
	}
	Expr or( Expr expr ){
		return new OrExpr( this, expr );		
	}
	Expr bbox( Envelope bbox ){
		return new BBoxExpr( bbox );
	}
	Expr literal( int i ){
		return new LiteralExpr( i );
	}
	Expr literal( double d ){
		return new LiteralExpr( d );
	}
	Expr literal( boolean b ){
		return new LiteralExpr( b );
	}
	Expr literal( Object literal ){
		return new LiteralExpr( literal );
	}	
	Expr add( Expr expr ){
		return new MathExpr( this, Expression.MATH_ADD, expr );
	}
	Expr subtract( Expr expr ){
		return new MathExpr( this, Expression.MATH_SUBTRACT, expr );
	}
	Expr divide( Expr expr ){
		return new MathExpr( this, Expression.MATH_DIVIDE, expr );
	}
	Expr multiply( Expr expr ){
		return new MathExpr( this, Expression.MATH_MULTIPLY, expr );
	}
	Expr attribute( String xpath ){
		return new AttributeExpr( xpath );
	}
	/** Convience attribute expression that retrieves default geometry */
	Expr geom(){
		return new GeomExpr();
	}
	Expr eq( Expr expr ){
		return new CompareExpr(this, Filter.COMPARE_EQUALS, expr );
	}
	Expr gt( Expr expr ){
		return new CompareExpr(this, Filter.COMPARE_GREATER_THAN, expr );
	}
	Expr gte( Expr expr ){
		return new CompareExpr(this, Filter.COMPARE_GREATER_THAN_EQUAL, expr );
	}
	Expr lt( Expr expr ){
		return new CompareExpr(this, Filter.COMPARE_LESS_THAN, expr );
	}
	Expr lte( Expr expr ){
		return new CompareExpr(this, Filter.COMPARE_LESS_THAN_EQUAL, expr );
	}
	Expr ne( Expr expr ){
		return new CompareExpr(this, Filter.COMPARE_NOT_EQUALS, expr );
	}
	Expr notNull(){
		return new NullExpr( this );
	}
	Expr between( Expr min, Expr max ){
		return new BetweenExpr( min, this, max );
	}
	Expr beyond( Expr expr, double distance ){
		return new GeometryDistanceExpr( this, Filter.GEOMETRY_BEYOND, expr, distance );
	}
	Expr contains( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_CONTAINS, expr );
	}
	Expr crosses( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_CROSSES, expr );
	}
	Expr disjoint( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_DISJOINT, expr );
	}	
	Expr dwithin( Expr expr, double distance ){
		return new GeometryDistanceExpr( this, Filter.GEOMETRY_DWITHIN, expr, distance );
	}
	Expr equal( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_EQUALS, expr );
	}
	Expr intersects( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_INTERSECTS, expr );
	}
	Expr overlaps( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_OVERLAPS, expr );
	}
	Expr touches( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_TOUCHES, expr );
	}
	Expr within( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_WITHIN, expr );
	}
	Expr fn( Expr expr ){
		return new GeometryExpr( this, Filter.GEOMETRY_WITHIN, expr );
	}
}
/**
 * Usefull super class that casts a Filter.contains( feature )
 * into a boolean Expression.
 * <p>
 * This allows you to implement Expressions for Filter opperations
 * without implementing the same code all the time.
 * </p> 
 */
class FilterExpr extends Expr {
	
	Expression expression(final FeatureType schema) {				
		return new Expression(){
			public short getType() {
				return 0;
			}
			public Object getValue(Feature feature) {
				Filter filter = filter( feature.getFeatureType() );
				return filter.contains( feature )
					? Boolean.TRUE
					: Boolean.FALSE;
			}
			public void accept(FilterVisitor visitor) {
				filter( schema ).accept( visitor );				
			}					
		};
	}
}
/**
 * Useful super class that casts expressions into a Filter.
 * <p>
 * To turn Expression into a Filter I am going to follow the usual
 * Perl defaults of "true".
 * <ul>
 * <li>Boolean.TRUE is true
 * <li>Non empty String is true
 * <li>Non zero is true
 * <li>Non zero length array is true
 * <li>Non isEmpty Collection/Map is true
 * <li>Non isNull Envelope is true
 * </ul>
 * Where true means that the generated Filter will accept the results.
 * </p>
 */
class ExpressionExpr extends Expr {
	
	Filter filter(final FeatureType schema){
		return new Filter(){
			public boolean contains(Feature feature) {
				Expression expression = expression( schema );
				Object value = expression.getValue( feature );
				if( value == null ) return false;
				if( value instanceof Boolean) {
					return ((Boolean)value).booleanValue();
				}
				if( value instanceof Number ){
					return ((Number)value).doubleValue() != 0.0;
				}
				if( value instanceof String ){
					return ((String)value).length() != 0; 
				}
				if( value.getClass().isArray() ){
					return Array.getLength( value ) != 0;
				}
				if( value instanceof Collection ){
					return !((Collection)value).isEmpty();
				}
				if( value instanceof Map ){
					return !((Map)value).isEmpty();
				}
				if( value instanceof Envelope ){
					return ((Envelope)value).isNull();						
				}
				return false;
			}
			public Filter and(Filter filter) {
				try {
					return factory.createLogicFilter( this, filter, Filter.LOGIC_AND );
				} catch (IllegalFilterException e) {
					return null;
				}				
			}

			public Filter or(Filter filter) {
				try {
					return factory.createLogicFilter( this, filter, Filter.LOGIC_OR );
				} catch (IllegalFilterException e) {
					return null;
				}
			}

			public Filter not() {
				try {
					return factory.createLogicFilter( this, Filter.LOGIC_NOT );
				} catch (IllegalFilterException e) {
					return null;
				}
			}

			public short getFilterType() {
				return 0;
			}
			public void accept(FilterVisitor visitor) {
				expression( schema ).accept( visitor );				
			}			
		};
	}
}
class FidsExpr extends FilterExpr {
	Set fids = new HashSet();
	FidsExpr( Collection fidCollection ){
		fids.addAll( fidCollection );
	}
	FidsExpr( String fid ){
		fids.add( fid );
	}
	Filter filter(FeatureType schema) {
		FidFilter filter = factory.createFidFilter();
		filter.addAllFids( fids );
		return filter;
	}
	Expr fid(Set moreFids ) {
		Set allFids = new HashSet();
		allFids.addAll( fids );
		allFids.addAll( moreFids );
		return new FidsExpr( allFids );
	}
	Expr fid(String featureID) {
		Set allFids = new HashSet();
		allFids.addAll( fids );
		allFids.add( featureID );
		return new FidsExpr( allFids );
	}
}
class NotExpr extends FilterExpr {
	Expr expr;
	NotExpr( Expr expr ){
		this.expr = expr;
	}
	Filter filter(FeatureType schema) {
		return expr.filter( schema ).not();
	}
}
class AndExpr extends FilterExpr {
	Expr expr1,expr2;
	AndExpr( Expr expr1, Expr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	Filter filter(FeatureType schema) {
		Filter filter1 = expr1.filter( schema );
		Filter filter2 = expr2.filter( schema );
		
		return filter1.and( filter2 );		
	}
}
class OrExpr extends FilterExpr {
	Expr expr1,expr2;
	OrExpr( Expr expr1, Expr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	Filter filter(FeatureType schema) {
		Filter filter1 = expr1.filter( schema );
		Filter filter2 = expr2.filter( schema );
		
		return filter1.or( filter2 );		
	}
}
class BBoxExpr extends ExpressionExpr {
	Envelope bbox;
	BBoxExpr( Envelope bbox ){
		this.bbox = bbox;		
	}
	Expression expression(FeatureType schema) {
		try {
			return factory.createBBoxExpression( bbox );		
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}
class LiteralExpr extends ExpressionExpr {
	Object literal;
	LiteralExpr( int i ){
		this( new Integer( i ) );		
	}
	LiteralExpr( double d ){
		this( new Double( d ) );		
	}
	LiteralExpr( boolean b ){
		this( b ? Boolean.TRUE : Boolean.FALSE );		
	}
	LiteralExpr( Object value ){
		literal = value;
	}
	Expression expression(FeatureType schema) {
		try {
			return factory.createLiteralExpression( literal );		
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}
class MathExpr extends ExpressionExpr {
	Expr expr1,expr2;
	short op;
	MathExpr( Expr expr1, short op, Expr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	Expression expression(FeatureType schema) {
		try {
			MathExpression math = factory.createMathExpression( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			math.addLeftValue( left );
			math.addRightValue( right );
			return math;
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}
class AttributeExpr extends ExpressionExpr {
	String path;
	AttributeExpr( String path ){
		this.path = path;
	}
	Expression expression(FeatureType schema) {
		try {
			return factory.createAttributeExpression( schema, path );			
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}
class GeomExpr extends ExpressionExpr {
	Expression expression(FeatureType schema) {
		try {
			return factory.createAttributeExpression( schema, schema.getDefaultGeometry().getName() );			
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}
class CompareExpr extends FilterExpr {
	Expr expr1,expr2;
	short op;
	CompareExpr( Expr expr1, short op, Expr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	Filter filter(FeatureType schema) {
		try {
			CompareFilter compare = factory.createCompareFilter( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			compare.addLeftValue( left );
			compare.addRightValue( right );
			return compare;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}
class NullExpr extends ExpressionExpr {
	Expr expr;
	NullExpr( Expr expr ){
		this.expr = expr;
	}
	Filter filter(FeatureType schema) {
		try {
			NullFilter nullFilter = factory.createNullFilter();
			nullFilter.nullCheckValue( expr.expression( schema ) );
			return nullFilter;
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}
class BetweenExpr extends FilterExpr {
	Expr expr, min, max;	
	BetweenExpr( Expr min, Expr expr, Expr max ){
		this.expr = expr;
		this.min = min;
		this.max = max;
	}
	Filter filter(FeatureType schema) {
		try {
			BetweenFilter between = factory.createBetweenFilter();
			Expression expression = expr.expression( schema );
			Expression left = min.expression( schema );
			Expression right = max.expression( schema );
			between.addMiddleValue( expression );
			between.addLeftValue( left );
			between.addRightValue( right );
			return between;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}
class GeometryExpr extends FilterExpr {
	Expr expr1,expr2;	
	short op;
	double distance;
	GeometryExpr( Expr expr1, short op, Expr expr2){
		this( expr1, op, expr2, Double.NaN );
	}
	GeometryExpr( Expr expr1, short op, Expr expr2, double distance ){
		this.expr1 = expr1;
		this.expr2 = expr2;
		this.op = op;
		this.distance = distance;
	}
	Filter filter(FeatureType schema) {
		try {
			GeometryFilter filter = factory.createGeometryFilter( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			filter.addLeftGeometry( left );
			filter.addRightGeometry( right );			
			return filter;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}
class GeometryDistanceExpr extends FilterExpr {
	Expr expr1,expr2;	
	short op;
	double distance;	
	GeometryDistanceExpr( Expr expr1, short op, Expr expr2, double distance ){
		this.expr1 = expr1;
		this.expr2 = expr2;
		this.op = op;
		this.distance = distance;
	}
	Filter filter(FeatureType schema) {
		try {
			GeometryDistanceFilter filter = factory.createGeometryDistanceFilter( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			filter.addLeftGeometry( left );
			filter.addRightGeometry( right );
			filter.setDistance( distance );
			return filter;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}

class FunctionExpr extends ExpressionExpr {
	Expr expr[];	
	String name;	
	FunctionExpr( String name, Expr expr ){		
		this.name = name;
		this.expr = new Expr[]{ expr, };
	}
	FunctionExpr( String name, Expr expr1, Expr expr2 ){		
		this.name = name;
		this.expr = new Expr[]{ expr1, expr2 };
	}
	FunctionExpr( String name, Expr expr[] ){		
		this.name = name;
		this.expr = expr;
	}
	Expression expression(FeatureType schema) {
		FunctionExpression fn = factory.createFunctionExpression( name );
		Expression args[] = new Expression[ expr.length ];
		for( int i=0; i<expr.length; i++ ){
			args[i] = expr[i].expression( schema );
		}			
		fn.setArgs( args );
		return fn;			
	}
}