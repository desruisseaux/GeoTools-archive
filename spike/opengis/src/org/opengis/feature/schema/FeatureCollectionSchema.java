package org.opengis.feature.schema;

import org.opengis.feature.type.FeatureCollectionType;

public interface FeatureCollectionSchema extends ComplexSchema {
	FeatureCollectionType getType();

	/**
	 * Schema of allowed members (or child content)
	 */
	Schema getMemberSchema();

}