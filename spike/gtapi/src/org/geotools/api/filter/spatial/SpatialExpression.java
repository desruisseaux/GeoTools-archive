package org.geotools.api.filter.spatial;

import org.geotools.api.filter.expression.Expression;
import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This is an expression which is known to result in a Spatial value.
 * <p>
 * Note: This is an additional to the origional GeoAPI interfaces allowing
 * us to be explicit (ie typesafe) when making expressions.
 * </p>
 * @author Jody Garnett
 */
public interface SpatialExpression extends Expression {
	/**
	 * SpatialExpression is known to evaulate to a Geometry.
	 * <p>
	 * Note: This is a Java5 type narrowing example - Java 1.4
	 * users can safely cast.
	 * </p>
	 */
	public Geometry evaluate(Feature feature);
}
