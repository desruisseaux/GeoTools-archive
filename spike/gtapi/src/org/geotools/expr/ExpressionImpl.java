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

 
 /**
  * Provides default Expression implementation with chaining
  * of implementations expression.
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
 abstract class ExpressionImpl {
	 
 	/** Factory used to create additional chained Expressions */
 	protected Expr factory;
 	
 	/**
 	 * To facilitate chaining a factory is required.
 	 * <p>
 	 * By convention this is the factory that was used to create
 	 * this object.
 	 * </p>
 	 * @param factory
 	 */
 	protected ExpressionImpl( Expr factory ){
 		this.factory = factory;
 	}
 	
 	//
 	// Convience methods
 	//
// 	/** Convience method for: geom().disjoint( geom( bbox )).not() */ 
// 	public Expr bbox( Envelope bbox ){
// 		return factory.bbox( )
// 	}
// 	
// 	/**
// 	 * Convience method for accessing a single fid
// 	 * 
// 	 * @param fetureID
// 	 * @return
// 	 */
// 	public Expr fid( final String featureID ){		
// 		Set set = new HashSet();
// 		set.add( featureID );
// 		return and( new FidsExpr( set ) );
// 	}
// 	//
// 	// Default Implemetnation
// 	//
// 	public Expr fid( final Set fids ){
// 		return and( new FidsExpr( fids ) );
// 	}	
// 	public Expr not(){
// 		return new NotExpr( this );
// 	}
// 	public Expr and( Expr expr ){
// 		return new AndExpr( this, expr );
// 	}
// 	public Expr or( Expr expr ){
// 		return new OrExpr( this, expr );		
// 	}
// 	
// 	public Expr eq( Expr expr ){
// 		return new CompareExpr(this, Filter.COMPARE_EQUALS, expr );
// 	}
// 	public Expr gt( Expr expr ){
// 		return new CompareExpr(this, Filter.COMPARE_GREATER_THAN, expr );
// 	}
// 	public Expr gte( Expr expr ){
// 		return new CompareExpr(this, Filter.COMPARE_GREATER_THAN_EQUAL, expr );
// 	}
// 	public Expr lt( Expr expr ){
// 		return new CompareExpr(this, Filter.COMPARE_LESS_THAN, expr );
// 	}
// 	public Expr lte( Expr expr ){
// 		return new CompareExpr(this, Filter.COMPARE_LESS_THAN_EQUAL, expr );
// 	}
// 	public Expr ne( Expr expr ){
// 		return new CompareExpr(this, Filter.COMPARE_NOT_EQUALS, expr );
// 	} 	
// 	public Expr between( Expr min, Expr max ){
// 		return new BetweenExpr( min, this, max );
// 	}
// 	
// 	public Expr notNull(){
// 		return new NullExpr( this );
// 	}
// 	public Expr fn( String name ){
// 		return new FunctionExpr( name, this );
// 	}
// 	public Expr fn( String name, Expr expr ){
// 		return new FunctionExpr( name, this, expr );
// 	}
// 	public Expr fn( String name, Expr expr[] ){
// 		Expr params[] = new Expr[ expr.length+1];
// 		params[0] = this;
// 		for( int i=0; i<expr.length;i++){
// 			params[i+1] = expr[i]; 			
// 		}
// 		return new FunctionExpr( name, params );
// 	}
 }











