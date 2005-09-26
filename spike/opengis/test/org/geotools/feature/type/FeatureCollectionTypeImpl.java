package org.geotools.feature.type;

import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

public class FeatureCollectionTypeImpl extends FeatureTypeImpl implements FeatureCollectionType {
	protected final Descriptor MEMBERDESCRIPTOR;

	public FeatureCollectionTypeImpl( QName name, Descriptor members, Descriptor schema, AttributeType defaultGeom) {
		super(name, schema, defaultGeom);
		MEMBERDESCRIPTOR = members;
	}
	public FeatureCollectionTypeImpl( QName name, Descriptor members, Descriptor schema, AttributeType defaultGeom, Set<Filter> restrictions, ComplexType superType, boolean isAbstract){
		super( name, schema, defaultGeom, restrictions, superType, isAbstract );
		MEMBERDESCRIPTOR = members;
	}	
	public Descriptor getMemberDescriptor() {
		return MEMBERDESCRIPTOR;
	}
}
