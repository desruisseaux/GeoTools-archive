/*
 * Created on Jun 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.expr;

/**
 * Expr known to be a mathmatical type.
 * <p>
 * Allows us to issolate all math opperations against a single
 * Expr subclass.
 * </p>
 */
public interface MathExpr extends Expr {
	public MathExpr add( MathExpr expr );
	public MathExpr add( Number number );
	public MathExpr add( double number );
	
	public MathExpr subtract( MathExpr expr );
	public MathExpr subtract( Number number );	
	public MathExpr subtract( double number );	
	
	public MathExpr divide( MathExpr expr );
	public MathExpr divide( Number number );
	public MathExpr divide( double number );
	
	public MathExpr multiply( MathExpr expr );
	public MathExpr multiply( Number number );
	public MathExpr multiply( double number );
	
	public Expr eq( Number number );	
	public Expr eq( double number );
	
	public Expr gt( Number number );
	public Expr gt( double number );
	
	public Expr gte( Number number );
	public Expr gte( double number );
	
	public Expr lt( Number number );
	public Expr lt( double number );
	
	public Expr lte( Number number );
	public Expr lte( double number );
	
	public Expr ne( Number number );
	public Expr ne( double  number );
	
	public Expr between( Number min, Number max  );
	public Expr between( double min, double max  );
}
