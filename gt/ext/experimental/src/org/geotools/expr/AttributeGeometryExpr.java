package org.geotools.expr;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;

public class AttributeGeometryExpr extends AbstractGeometryExpr {
	String attribute;
	
	public AttributeGeometryExpr(){
		this( null );
	}
	public AttributeGeometryExpr( String attribute ){
		this.attribute = attribute;
	}
	public Expression expression(FeatureType schema) {
		try {
			String path = attribute != null
				? attribute
				: schema.getDefaultGeometry().getName();
			return factory.createAttributeExpression( schema, path );			
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}