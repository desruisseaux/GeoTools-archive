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
package org.geotools.expr;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geotools.data.Extent;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;

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
 * Example Expr:<br>
 * <code>Exprs.bbox( extent ).and( Exprs.attribute("cost").lt( 50 ) )</code>
 * </p>
 * <p>
 * <p>
 * Example Filter:<br>
 * <pre><code>
 * Expression extentExpression = factory.createBBoxExpression( bbox );
 * Expression geomExpression = factory.createAttributeExpression( featureType, featureType.getDefaultGeometry().getName() );	    
 * GeometryFilter disjointExpression = factory.createGeometryFilter( GeometryFilter.GEOMETRY_DISJOINT );
 * disjointExpression.addLeftGeometry( geomExpression );
 * disjointExpression.addRightGeometry( extentExpression );	    
 * Filter bboxFilter = disjointExpression.not();	    
 * AttributeExpression costExpression = factory.createAttributeExpression( featureType, "cost" );
 * CompareFilter lessThanFilter = factory.createCompareFilter( CompareFilter.COMPARE_LESS_THAN );
 * lessThanFilter.addLeftValue( costExpression );
 * lessThanFilter.addRightValue( factory.createLiteralExpression( 50 ) );	    
 * LogicFilter filter = factory.createLogicFilter( bboxFilter, lessThanFilter, LogicFilter.LOGIC_AND);	    
 * </code></pre>
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
	static public Expr literal( boolean b ){
		return new LiteralExpr( b );
	}
	static public MathExpr literal( int number ){
		return new LiteralMathExpr( number );
	}
	static public MathExpr literal( double number ){
		return new LiteralMathExpr( number );
	}
	static public MathExpr literal( Number number ){
		return new LiteralMathExpr( number );
	}	
	static public Expr literal( Object literal ){
		return new LiteralExpr( literal );
	}
	/**
	 * Literal Geometry access.
	 */
	static public GeometryExpr literal( Envelope extent ){
		return new LiteralGeometryExpr( extent );
	}
	/**
	 * Literal Geometry access.
	 */
	static public GeometryExpr literal( Geometry geom ){
		return new LiteralGeometryExpr( geom );
	}	
	static public MathExpr add( MathExpr expr[] ){
		if( expr.length == 0 ) return literal( 0 );
		if( expr.length == 1 ) return expr[0];
		MathExpr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.add( expr[i] );
		}
		return e;
	}
	static public MathExpr subtract( MathExpr expr[] ){
		if( expr.length == 0 ) return literal( 0 );
		if( expr.length == 1 ) return expr[0];
		MathExpr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.subtract( expr[i] );
		}
		return e;
	}
	static public MathExpr divide( MathExpr expr[] ){
		if( expr.length == 0 ) return literal( 1 );
		if( expr.length == 1 ) return expr[0];
		MathExpr e = expr[0];
		for( int i=1; i<expr.length;i++){
			e = e.divide( expr[i] );
		}
		return e;
	}
	static public Expr multiply( MathExpr expr[] ){
		if( expr.length == 0 ) return literal( 1 );
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
	 * Metadata element access.
	 * <p>
	 * Allows access to chained opperations.
	 * <p>
	 * @param attribute xpath expression to metadata element
	 * @return Expr
	 */
	static public Expr meta( String xpath ){
		return new MetadataExpr( xpath );
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
	static public MathExpr math( String attribute ){
		return new AttributeMathExpr( attribute );
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
 	/**
 	 * Defines the usual Perl concept of "true".
 	 * <ul>
 	 * <li>Boolean.TRUE is true
 	 * <li>Non empty String is true
 	 * <li>Non zero is true
 	 * <li>Non zero length array is true
 	 * <li>Non isEmpty Collection/Map is true
 	 * <li>Non isNull Envelope is true
 	 * </ul>
 	 * @value value To be examined for truthfulness
 	 * @return the truth is out there
 	 */ 	
 	static boolean truth( Object value ){ 		
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
}











