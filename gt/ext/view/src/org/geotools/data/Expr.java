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
import java.util.Set;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;

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
 * </p>
 * <p>
 * BTW: just so we can have everything make sense
 * <ul>
 * <li> Expr is immutable
 * <li> I don't care if Expr trees simplfy themselves during construction
 * <li> If you are going to simply - '.' means AND - <br>
 *      <code>Expr.bbox( extent ).lt( Expr.attribute("cost"), 50 ) ==
 *  	Expr.bbox( extent ).and( lt( Expr.attribute("cost"), 50 ) )</code>
 * <li> There are convience methods that make sense - <br>
 * 		<code>Expr.bbox( extent ) == geom().disjoint( Expr.literal( extent)).not()</code>
 * <li> Or you have to do by hand - <br>
 *      <code>Expr.fid( "road.1234" ).or( Expr.fid("road.4321) )</code>
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
interface Expr {	
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
	
	// Standard Expr chains
	/** this + expr */
	public Expr add( Expr expr );
	/** this - expr */
	public Expr subtract( Expr expr );
	/** this / expr */
	public Expr divide( Expr expr );
	public Expr multiply( Expr expr );		
	public Expr eq( Expr expr );
	public Expr gt( Expr expr );
	public Expr gte( Expr expr );
	public Expr lt( Expr expr );
	public Expr lte( Expr expr );
	public Expr ne( Expr expr );
	public Expr notNull();
	public Expr between( Expr min, Expr max );
	public Expr beyond( Expr expr, double distance );
	public Expr contains( Expr expr );
	public Expr crosses( Expr expr );
	public Expr disjoint( Expr expr );	
	public Expr dwithin( Expr expr, double distance );
	public Expr equal( Expr expr );
	public Expr intersects( Expr expr );
	public Expr overlaps( Expr expr );
	public Expr touches( Expr expr );
	public Expr within( Expr expr );
 	public Expr fn( String name );
 	public Expr fn( String name, Expr expr );
 	public Expr fn( String name, Expr expr[] );
}











