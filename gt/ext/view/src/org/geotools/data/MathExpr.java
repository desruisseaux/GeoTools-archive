/*
 * Created on Jun 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data;

/**
 * Expr known to be a mathmatical type.
 * <p>
 * Allows us to issolate all math opperations against a single
 * Expr subclass.
 * </p>
 */
public interface MathExpr extends Expr {
	public MathExpr add( MathExpr expr );
	public MathExpr subtract( MathExpr expr );
	public MathExpr divide( MathExpr expr );
	public MathExpr multiply( MathExpr expr );
}
