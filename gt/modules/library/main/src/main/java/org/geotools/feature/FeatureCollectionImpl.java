package org.geotools.feature;

import java.util.Collection;

import org.opengis.feature.FeatureCollection;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureCollectionType;

public abstract class FeatureCollectionImpl extends FeatureImpl implements
		FeatureCollection {

	public FeatureCollectionImpl(Collection values, AttributeDescriptor desc, String id) {
		super(values,desc,id);
	}

	public FeatureCollectionImpl(Collection values, FeatureCollectionType type, String id) {
		super(values,type,id);
	}
	
	public Collection memberTypes() {
        return ((FeatureCollectionType) getType()).getMembers();
    }

}
