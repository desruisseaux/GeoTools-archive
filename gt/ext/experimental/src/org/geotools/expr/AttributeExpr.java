package org.geotools.expr;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;

/**
 * Retrive attribute from Feature.
 * <p>
 * This is a start of an Expr chain - I would rather setup
 * Expr.attribute( path ) if I can figure out how.
 * </p>
 */
public class AttributeExpr extends AbstractExpr {
	String path;	
	public AttributeExpr( String path ){
		this.path = path;
	}
	public Expression expression(FeatureType schema) {
		try {
			return factory.createAttributeExpression( schema, path );			
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}