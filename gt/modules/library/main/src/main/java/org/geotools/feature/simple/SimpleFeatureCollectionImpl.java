package org.geotools.feature.simple;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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
	    SimpleFeatureCollectionType collectionType = (SimpleFeatureCollectionType) getType();
	    return collectionType.getMemberType();
//		Set<AssociationDescriptor> members = ((FeatureCollectionType) getType()).getMembers();
//		
//        AssociationDescriptor member = (AssociationDescriptor) members.iterator().next(); 
//		return (SimpleFeatureType) member.getType().getReferenceType();
	}
	
	public Collection memberTypes() {
        SimpleFeatureCollectionType collectionType = (SimpleFeatureCollectionType) getType();
        return collectionType.getMemberTypes();
		//return Collections.singleton(getMemberType());
	}
	
}
