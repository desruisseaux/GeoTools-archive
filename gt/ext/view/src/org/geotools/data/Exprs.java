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

import java.util.HashSet;
import java.util.Set;

import org.geotools.filter.FilterFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Filter/Expression construction kit - this class starts off chains.
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
 * On the way there we are not going to have the difference between
 * Filter/Expression. Expr can make an Expression, and Expr can make a
 * Filter.
 * </p>
 * <p>
 * Example:<br>
 * <code>Exprs.bbox( extent ).and( Exprs.attribute("cost").lt( 50 ) )</code>
 * </p>
 * <p>
 * BTW: just so we can have everything make sense
 * <ul>
 * <li> Expr is immutable
 * <li> I don't care if Expr trees simplfy themselves during construction
 * <li> Allow user to type less by using '.' to mean AND - <br>
 *      <code>Exprs.bbox( extent ).fids( fidSet) ==
 *  	Exprs.bbox( extent ).and( Exprs.fids( fidSet ) )
 * <li> There are convience methods that make sense - <br>
 * 		<code>Exprs.bbox( extent ) == geom().disjoint( Expr.literal( extent)).not()</code>
 * <li> "Or" you have to do by hand - <br>
 *      <code>Exprs.fid( "road.1234" ).or( Exprs.fid("road.4321) )</code>
 * </ul>
 * </p>
 * <p>
 * Wild idea time: chaining parameters
 * <ul> 
 * <li> Fid expr above with param chainging? <br>
 * 		<code>Expr.fid( "road.1234" ).param( "road.4321" )</code><br>
 * 		Of course this idea is really lame given 1.5 new features - kill it :-(
 * <li>Of course something like this may be Extactly whtat is needed to make
 *     those nasty As FeatureView/Opperation chains work....<br>
 *     <code>repository.view("road").as("name", "CUSTOMER_NAME").as( "addresss", "ADDRESS" )<code></br>
 *     Still does not look right - sigh.
 * 
 * <ul>
 * </p>
 * @author Jody Garnett
 */
public class Exprs {
	static protected FilterFactory factory = FilterFactory.createFilterFactory();
		
	/** Convience method for: geom().disjoint( literal( bbox )).not() */ 
	static public Expr bbox( Envelope bbox ){
		return geom().disjoint( geom( bbox )).not();		
	}
	/**
	 * Convience method for accessing a single fid
	 * 
	 * @param fetureID
	 * @return
	 */
	static public Expr fid( final String featureID ){		
		Set set = new HashSet();
		set.add( featureID );
		return new FidsExpr( set );		
	}
	//
	// Default Implemetnation
	//	
	static public Expr fid( final Set fids ){
		return new FidsExpr( fids );
	}			
	static public Expr and( Expr expr[] ){
		if( expr.length == 0 ) return literal( false );
		if( expr.length == 1 ) return expr[0];
		Expr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.and( expr[i] );
		}
		return e;		
	}	
	static public Expr or( Expr expr[] ){
		if( expr.length == 0 ) return literal( true );
		if( expr.length == 1 ) return expr[0];
		Expr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.or( expr[i] );
		}
		return e;
	}
	
	static public Expr literal( int i ){
		return new LiteralExpr( i );
	}
	static public Expr literal( double d ){
		return new LiteralExpr( d );
	}	
	static public Expr literal( boolean b ){
		return new LiteralExpr( b );
	}
	static public Expr literal( Object literal ){
		return new LiteralExpr( literal );
	}	
	static public MathExpr add( MathExpr expr[] ){
		if( expr.length == 0 ) return math( 0 );
		if( expr.length == 1 ) return expr[0];
		MathExpr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.add( expr[i] );
		}
		return e;
	}
	static public MathExpr subtract( MathExpr expr[] ){
		if( expr.length == 0 ) return math( 0 );
		if( expr.length == 1 ) return expr[0];
		MathExpr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.subtract( expr[i] );
		}
		return e;
	}
	static public MathExpr divide( MathExpr expr[] ){
		if( expr.length == 0 ) return math( 1 );
		if( expr.length == 1 ) return expr[0];
		MathExpr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.divide( expr[i] );
		}
		return e;
	}
	static public Expr multiply( MathExpr expr[] ){
		if( expr.length == 0 ) return math( 1 );
		if( expr.length == 1 ) return expr[0];
		MathExpr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.multiply( expr[i] );
		}
		return e;
	}
	
	/**
	 * Attribute access.
	 * <p>
	 * Allows access to chained opperations.
	 * <p>
	 * @param attribute xpath expression to attribute
	 * @return Expr
	 */
	static public Expr attribute( String attribute ){
		return new AttributeExpr( attribute );
	}

	/**
	 * Default Geometry access.
	 */
	static public GeometryExpr geom(){
		return new AttributeGeometryExpr();
	}
	
	/**
	 * Geometry attribute access.
	 * <p>
	 * Allows access to chained spatial opperations.
	 * <p>
	 * @param attribute xpath expression to attribute
	 * @return GeometryAttribtue
	 */
	static public GeometryExpr geom( String attribute ){
		return new AttributeGeometryExpr( attribute );
	}
	/**
	 * Literal Geometry access.
	 */
	static public GeometryExpr geom( Envelope extent ){
		return new LiteralGeometryExpr( extent );
	}
	/**
	 * Literal Geometry access.
	 */
	static public GeometryExpr geom( Geometry geom ){
		return new LiteralGeometryExpr( geom );
	}
	
	static public MathExpr math( String attribute ){
		return new AttributeMathExpr( attribute );
	}
	static public MathExpr math( int number ){
		return new LiteralMathExpr( number );
	}
	static public MathExpr math( double number ){
		return new LiteralMathExpr( number );
	}
	static public MathExpr math( Number number ){
		return new LiteralMathExpr( number );
	}
	
	static public Expr fn( String name, Expr expr ){
 		return new FunctionExpr( name, expr );
 	}
	static public Expr fn( String name, Expr expr1, Expr expr2 ){
 		return new FunctionExpr( name, expr1, expr2 );
 	}
 	static public Expr fn( String name, Expr expr[] ){
 		return new FunctionExpr( name, expr );
 	}
}











