package org.geotools.api.filter.expression;

import org.opengis.feature.Feature;

/**
 * This is an expression which is known to result in a number.
 * <p>
 * Note: This is an additional to the origional GeoAPI interfaces allowing
 * us to be explicit (ie typesafe) when making expressions.
 * </p>
 * @author Jody Garnett
 */
public interface MathExpression extends Expression {
	/**
	 * MathExpression is known to evaulate to a Number.
	 * <p>
	 * Note: This is a Java5 type narrowing example - Java 1.4
	 * users can safely cast.
	 * </p>
	 */
	public Number evaluate(Feature feature);
}
