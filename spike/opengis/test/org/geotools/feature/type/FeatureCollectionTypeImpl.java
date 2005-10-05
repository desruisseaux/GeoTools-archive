package org.geotools.feature.type;

import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;

public class FeatureCollectionTypeImpl extends FeatureTypeImpl implements FeatureCollectionType {
	protected final Descriptor MEMBERDESCRIPTOR;

	public FeatureCollectionTypeImpl( QName name, Descriptor members, Descriptor schema, GeometryType defaultGeom) {
		super(name, schema, defaultGeom);
		MEMBERDESCRIPTOR = members;
	}
	public FeatureCollectionTypeImpl( QName name, Descriptor members, Descriptor schema, GeometryType defaultGeom, Set<Filter> restrictions, FeatureCollectionType superType, boolean isAbstract){
		super( name, schema, defaultGeom, restrictions, superType, isAbstract );
		MEMBERDESCRIPTOR = members;
	}	
	
	public Descriptor getMemberDescriptor() {
		return MEMBERDESCRIPTOR;
	}
}
