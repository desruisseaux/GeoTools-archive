package org.opengis.feature.schema;

import org.opengis.feature.type.FeatureType;

public interface FeatureSchema extends ComplexSchema {
	FeatureType getType();
}