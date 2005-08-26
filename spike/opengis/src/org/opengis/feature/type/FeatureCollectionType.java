package org.opengis.feature.type;


public interface FeatureCollectionType extends FeatureType {
	/**
	 * Allowable "child" FeatureType.
	 * 
	 * Once again this information may be considered derrived from
	 * getMemberSchema().
	 */
	FeatureType getMemberType();
}