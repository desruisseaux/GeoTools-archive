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
import java.util.Set;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.metadata.Metadata;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Filter/Expression construction kit - this class forms chains.
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
 * <li> Or you have to do by hand - <br>
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
 * <ul>
 * </p>
 * @author Jody Garnett
 */
public interface Expr {
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
	public Expr resolve( Metadata metadata );
	
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
	public Expr reduce( String bind );
	/**
	 * Bind attributes matching "bind/**" to feature.getAttribute("**).
	 * <p>
	 * This may be used to reduce an Expr as part of an otter join, to
	 * something simple that can be passed off to an DataStore by way
	 * of filter( FeatureType ).
	 * </p>
	 * <p>
	 * This method will also try and reduce any metadata xpath
	 * expression matching "bind/**".
	 * </p>
	 * @param bind
	 * @param feature
	 * @return Expr modified with literals in place of "bind/**" attributes.
	 */
	public Expr resolve( String bind, Feature feature );
	
	/** Lazy binding of Expr into a Filter */
	public Filter filter(FeatureType schema) throws IOException;
	
	/** Lazy binding of Expr into an Expression */
	public Expression expression( FeatureType schema ) throws IOException;
	
	/**
	 * expr.and( geom().bbox().disjoint( extent ).not )
	 * <p>
	 * Restrict current Expr with provided bounding box
	 * </p>
	 */
	public Expr bbox( Envelope extent );
	
	/**
	 * expr.filter( fid )
	 * <p>
	 * Restruct current Expr to provided featureId(s).
	 * </p>
	 */
	public Expr fid( final String featureID );
	/**
	 * expr.filter( fids )
	 * <p>
	 * Restruct current Expr to provided featureId(s).
	 * </p>
	 */
	public Expr fid( final Set fids );
	
	/**
	 * !expr
	 * <p>
	 * Invert of logical TRUTH value. Can be used to test for non contained fitler contents or
	 * FALSE Expressions
	 * </p>
	 * @return Invert of Logical TRUETH value
	 */
	public Expr not();
	/**
	 * Explicit Expr chain extention - required for custom Expressions.
	 * 
	 * @param expr
	 * @return Explicit chain extention.
	 */
	public Expr and( Expr expr );
	
	/** Expr Disjunction */
	public Expr or( Expr expr );
	
	/** expr == expr */
	public Expr eq( Expr expr );	
	/** expr > expr */
	public Expr gt( Expr expr );	
	/** expr >= expr */
	public Expr gte( Expr expr );
	/** expr < expr */
	public Expr lt( Expr expr );
	/** expr <= expr */
	public Expr lte( Expr expr );
	/** expr != expr */
	public Expr ne( Expr expr );
	/** min <= expr <= max */
	public Expr between( Expr min, Expr max  );
	
	/** expr != null */
	public Expr notNull();
	
	/** name( expr ) */
 	public Expr fn( String name );
 	/** name( expr, expr ) */
 	public Expr fn( String name, Expr expr );
 	/** name( expr[0], expr[0], ... ) */
 	public Expr fn( String name, Expr expr[] );
}











