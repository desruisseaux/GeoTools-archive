package org.geotools.data;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;

public class GeomExpr extends AbstractExpressionExpr {
	public GeomExpr(){
	}
	public Expression expression(FeatureType schema) {
		try {
			return factory.createAttributeExpression( schema, schema.getDefaultGeometry().getName() );			
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}