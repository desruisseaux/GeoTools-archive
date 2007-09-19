package org.geotools.feature;

import org.geotools.feature.simple.SimpleFeatureBuilder;

public class DefaultFeatureBuilder extends SimpleFeatureBuilder {

	public DefaultFeatureBuilder() {
		super( new DefaultFeatureFactory() );
	}
}
