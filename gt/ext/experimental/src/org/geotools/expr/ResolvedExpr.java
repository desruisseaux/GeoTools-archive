package org.geotools.expr;

/**
 * Used to mark expression that have been resolved to a constant.
 * <p>
 * Not the sudden shift to bean naming conventions, getValue is
 * treated as an attribute, previously we have always been playing
 * with opperations.
 * </p>
 */
public interface ResolvedExpr {
	/**
	 * Value of this Expr.
	 * <p>
	 * Expr is doing its best to be immutable, Please 
	 * don't duck around this idea.
	 * </p>
	 * @return value of this Expr
	 */
	public Object getValue();

}
