package org.geotools.feature.simple;

import java.util.Collection;
import java.util.Collections;

import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureCollectionType;

public abstract class SimpleFeatureCollectionImpl extends SimpleFeatureImpl
	implements SimpleFeatureCollection {

	public SimpleFeatureCollectionImpl(AttributeDescriptor descriptor, String id) {
		super(Collections.EMPTY_LIST, descriptor, id);
	}
	
	public SimpleFeatureCollectionImpl(SimpleFeatureCollectionType type, String id) {
		super(Collections.EMPTY_LIST, type, id);
	}

	public SimpleFeatureCollectionType getFeatureCollectionType() {
		return (SimpleFeatureCollectionType)getType();
	}
	
	public SimpleFeatureType getMemberType() {
		AssociationDescriptor member = 
			(AssociationDescriptor) ((FeatureCollectionType) getType()).getMembers().iterator().next(); 
		return (SimpleFeatureType) member.getType().getReferenceType();
	}
	
	public Collection memberTypes() {
		return Collections.singleton(getMemberType());
	}
	
}
