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

 import java.io.IOException;
import java.lang.reflect.Array;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;

 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureType;
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
  * Provides default Expr chaining implementations expression.
  * <p>
  * This class provides the obious methods for all Expr methods. 
  * Obvious in thise case means that for any Expr method 
  * the appropriate constructor will be called and the resulting Exp
  * will be joined using a logical <code>AND</code> opperation.
  * </p>
  * <p> 
  * <code>OR</code> & <code>NOT</code> are the notiable notable
  * exceptions to this rule.
  * </p>
  * <p>
  * Please choose either AbstractExpresssionExpr, or
  * AbstractFilterExpr for your own custom Expr implementations.
  * These classes offer a better starting point the the root Expr
  * class (since it is modeled to assume the Expr value of TRUE) so
  * chainingwise its various methods act as constructors since
  * TRUE AND newX  is the same as newX.
  * </p>
  * @author Jody Garnett
  */
 abstract class AbstractExpr implements Expr {
 	protected FilterFactory factory = FilterFactory.createFilterFactory();
 	
 	/* Subclass must implement */
 	public abstract Filter filter(FeatureType schema) throws IOException;
 	/* Subclass must implement */
 	public abstract Expression expression( FeatureType schema ) throws IOException;
 		
 	//
 	// Convience methods
 	//
 	/** Convience method for: geom().disjoint( literal( bbox )).not() */ 
 	public Expr bbox( Envelope bbox ){
 		return and( geom().disjoint( literal( bbox )).not() );		
 	}
 	/**
 	 * Convience method for accessing a single fid
 	 * 
 	 * @param fetureID
 	 * @return
 	 */
 	public Expr fid( final String featureID ){		
 		Set set = new HashSet();
 		set.add( featureID );
 		return and( new FidsExpr( set ) );
 	}
 	//
 	// Default Implemetnation
 	//
 	public Expr fid( final Set fids ){
 		return and( new FidsExpr( fids ) );
 	}	
 	public Expr not(){
 		return new NotExpr( this );
 	}
 	public Expr and( Expr expr ){
 		return new AndExpr( this, expr );
 	}
 	public Expr or( Expr expr ){
 		return new OrExpr( this, expr );		
 	} 	 	
 	/**
 	 * Note literal is non chainging.
 	 * 
 	 * Should move this to a Chain class.
 	 */
 	public Expr literal( int i ){
 		return new LiteralExpr( i );
 	}
 	public Expr literal( double d ){
 		return new LiteralExpr( d );
 	}
 	public Expr literal( boolean b ){
 		return new LiteralExpr( b );
 	}
 	public Expr literal( Object literal ){
 		return new LiteralExpr( literal );
 	} 	
 	public Expr add( Expr expr ){
 		return new MathExpr( this, Expression.MATH_ADD, expr );
 	}
 	public Expr subtract( Expr expr ){
 		return new MathExpr( this, Expression.MATH_SUBTRACT, expr );
 	}
 	public Expr divide( Expr expr ){
 		return new MathExpr( this, Expression.MATH_DIVIDE, expr );
 	}
 	public Expr multiply( Expr expr ){
 		return new MathExpr( this, Expression.MATH_MULTIPLY, expr );
 	}
 	/** Non chaining */
 	public Expr attribute( String xpath ){
 		return new AttributeExpr( xpath );
 	}
 	/** Non chaining */
 	public Expr geom(){
 		return new GeomExpr();
 	} 	
 	public Expr eq( Expr expr ){
 		return new CompareExpr(this, Filter.COMPARE_EQUALS, expr );
 	}
 	public Expr gt( Expr expr ){
 		return new CompareExpr(this, Filter.COMPARE_GREATER_THAN, expr );
 	}
 	public Expr gte( Expr expr ){
 		return new CompareExpr(this, Filter.COMPARE_GREATER_THAN_EQUAL, expr );
 	}
 	public Expr lt( Expr expr ){
 		return new CompareExpr(this, Filter.COMPARE_LESS_THAN, expr );
 	}
 	public Expr lte( Expr expr ){
 		return new CompareExpr(this, Filter.COMPARE_LESS_THAN_EQUAL, expr );
 	}
 	public Expr ne( Expr expr ){
 		return new CompareExpr(this, Filter.COMPARE_NOT_EQUALS, expr );
 	}
 	public Expr notNull(){
 		return new NullExpr( this );
 	}
 	public Expr between( Expr min, Expr max ){
 		return new BetweenExpr( min, this, max );
 	}
 	public Expr beyond( Expr expr, double distance ){
 		return new GeometryDistanceExpr( this, Filter.GEOMETRY_BEYOND, expr, distance );
 	}
 	public Expr contains( Expr expr ){
 		return new GeometryExpr( this, Filter.GEOMETRY_CONTAINS, expr );
 	}
 	public Expr crosses( Expr expr ){
 		return new GeometryExpr( this, Filter.GEOMETRY_CROSSES, expr );
 	}
 	public Expr disjoint( Expr expr ){
 		return new GeometryExpr( this, Filter.GEOMETRY_DISJOINT, expr );
 	}	
 	public Expr dwithin( Expr expr, double distance ){
 		return new GeometryDistanceExpr( this, Filter.GEOMETRY_DWITHIN, expr, distance );
 	}
 	public Expr equal( Expr expr ){
 		return new GeometryExpr( this, Filter.GEOMETRY_EQUALS, expr );
 	}
 	public Expr intersects( Expr expr ){
 		return new GeometryExpr( this, Filter.GEOMETRY_INTERSECTS, expr );
 	}
 	public Expr overlaps( Expr expr ){
 		return new GeometryExpr( this, Filter.GEOMETRY_OVERLAPS, expr );
 	}
 	public Expr touches( Expr expr ){
 		return new GeometryExpr( this, Filter.GEOMETRY_TOUCHES, expr );
 	}
 	public Expr within( Expr expr ){
 		return new GeometryExpr( this, Filter.GEOMETRY_WITHIN, expr );
 	}
 	public Expr fn( String name ){
 		return new FunctionExpr( name, this );
 	}
 	public Expr fn( String name, Expr expr ){
 		return new FunctionExpr( name, this, expr );
 	}
 	public Expr fn( String name, Expr expr[] ){
 		Expr params[] = new Expr[ expr.length+1];
 		params[0] = this;
 		for( int i=0; i<expr.length;i++){
 			params[i+1] = expr[i]; 			
 		}
 		return new FunctionExpr( name, params );
 	}
 }











