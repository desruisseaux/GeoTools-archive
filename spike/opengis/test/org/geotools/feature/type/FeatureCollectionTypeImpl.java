package org.geotools.feature.type;

import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.filter.Filter;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;

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

	/**
	 * Intended for use only by factory providing abstrac Feature supertype
	 * @param name
	 * @param members
	 * @param schema
	 * @param defaultGeom
	 * @param restrictions
	 * @param superType
	 * @param isAbstract
	 */
	FeatureCollectionTypeImpl( QName name, Descriptor members, Descriptor schema, GeometryType defaultGeom, Set<Filter> restrictions, FeatureType superType, boolean isAbstract){
		super( name, schema, defaultGeom, restrictions, superType, isAbstract );
		MEMBERDESCRIPTOR = members;
	}	

	public Descriptor getMemberDescriptor() {
		return MEMBERDESCRIPTOR;
	}
}
