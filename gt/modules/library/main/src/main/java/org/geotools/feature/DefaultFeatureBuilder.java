package org.geotools.feature;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;

public class DefaultFeatureBuilder extends SimpleFeatureBuilder {

	public DefaultFeatureBuilder() {
		super( new DefaultFeatureFactory() );
	}
	
	protected boolean isGeometry(AttributeDescriptor value) {
		return value instanceof GeometryAttributeType;
	}
	
}
