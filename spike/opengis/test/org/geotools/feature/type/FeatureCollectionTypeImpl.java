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
	protected final Collection<FeatureType> MEMBERTYPES;

	public FeatureCollectionTypeImpl( QName name, Collection<FeatureType> memberTypes, Descriptor schema, AttributeType defaultGeom) {
		super(name, schema, defaultGeom);
		MEMBERTYPES = memberTypes;
	}
	public FeatureCollectionTypeImpl( QName name, Collection<FeatureType> memberTypes, Descriptor schema, AttributeType defaultGeom, Set<Filter> restrictions, ComplexType superType, boolean isAbstract){
		super( name, schema, defaultGeom, restrictions, superType, isAbstract );
		MEMBERTYPES = memberTypes;
	}
	public Collection<FeatureType> getMemberType() {
		return MEMBERTYPES;
	}
}
