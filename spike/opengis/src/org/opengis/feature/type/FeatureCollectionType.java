package org.opengis.feature.type;

import java.util.Collection;


public interface FeatureCollectionType extends FeatureType {	
	/**
	 * FeatureTypes allowable as members of this collection.
	 */
	Collection<FeatureType> getMemberDescriptor();
}