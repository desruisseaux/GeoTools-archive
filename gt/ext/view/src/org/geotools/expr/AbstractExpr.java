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

 import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.IllegalFilterException;
import org.geotools.metadata.Metadata;

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
	/**
	 * Bind all meta entries according to provided metadata.
	 * <p>
	 * <pre><code>
	 * Feature
	 * </code></pre>
	 * </p>
	 * @param metadata
	 * @return Expr with all meta( xpath ) Exprs resolved
	 */
	public Expr resolve( Metadata metadata ){
		return this;
	}
	
	/**
	 * 
	 * Reduce attributes matching "bind/x" to "x".
	 * <p>
	 * This may be used to reduce an Expr as part of an otter join, to
	 * something simple that can be passed off to an DataStore by way
	 * of filter( FeatureType ).
	 * </p>
	 * <p>
	 * Example:
	 * <pre><code>
	 * FeatureType RIVER = river.getSchema();
	 * FeatureType HAZZARD = hazard.getSchema();
	 * 
	 * Expr joinExpr = Exprs.attribute("river/name").eq( Exprs.attribute("hazzard/river") );
	 * 
	 * FeatureReader outer = river.getFeatures().reader();
	 * while( reader.hasNext() ){
	 *   Feature aRiver = outer.next();
	 * 
	 *   Expr inner = joinExpr.resolve( "river", aRiver ).reduce( "hazzard" );
	 *   FeatureReader inner = district.getFeatures( inner.filter( HAZZARD ) );
	 * 	 while( inner.hasNext() ){
	 *      Feature aHazzard = inner.next();
	 * 
	 * 		// code here has access to both aRiver and aHazzard 
	 *   } 
	 *   inner.close();
	 * }
	 * outer.close();
	 * </code></pre>
	 * </p>
	 */
	public Expr reduce( String bind ){
		return this;
	}
	/**
	 * Bind attributes matching "bind/x" to feature.getAttribute("x").
	 * <p>
	 * This may be used to reduce an Expr as part of an otter join, to
	 * something simple that can be passed off to an DataStore by way
	 * of filter( FeatureType ).
	 * </p>
	 * @param bind
	 * @param feature
	 * @return
	 */
	public Expr resolve( String bind, Feature feature ){
		return this;
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
	public Filter filter(final FeatureType schema) throws IOException {
		return new Filter(){
			public boolean contains(Feature feature) {
				Expression expression;
				try {
					expression = expression( schema );
				} catch (IOException e) {
					return false;
				}
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
				return 0; // TODO: What is the value for "custom"?
			}
			public void accept(FilterVisitor visitor) {
				try {
					expression( schema ).accept( visitor );
				} catch (IOException e) {					
				}				
			}			
		};
	}
	/**
	 * Turns a Filter into an a logical true/false expression.
	 */
	public Expression expression(final FeatureType schema) throws IOException {	
		/** Boolean expression based on Filter.contains */
		return new Expression(){
			/**
			 * Consider this a Function that
			 * returns filter.contains( feature )
			 */
			public short getType() {
				return Expression.FUNCTION;
			}
			public Object getValue(Feature feature) {
				boolean contains;
				try {
					Filter filter = filter( feature.getFeatureType() );
					contains = filter.contains( feature );
				}
				catch( IOException ignore ){
					contains = false;
				}
				return contains ? Boolean.TRUE : Boolean.FALSE;
			}
			public void accept(FilterVisitor visitor) {
				try {
					filter( schema ).accept( visitor );
				} catch (IOException e) {
				}				
			}					
		};
	} 		
 	//
 	// Convience methods
 	//
 	/** Convience method for: geom().disjoint( geom( bbox )).not() */ 
 	public Expr bbox( Envelope bbox ){
 		return and( Exprs.geom().disjoint( Exprs.literal( bbox )).not() );		
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
 	public Expr between( Expr min, Expr max ){
 		return new BetweenExpr( min, this, max );
 	}
 	
 	public Expr notNull(){
 		return new NullExpr( this );
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











