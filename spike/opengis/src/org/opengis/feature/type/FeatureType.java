package org.opengis.feature.type;

public interface FeatureType extends ComplexType {
	boolean isAbstract();

	FeatureType getSuper();
}