package org.geotools.feature.type;

import java.util.Collection;
import java.util.Set;

import org.opengis.feature.schema.Schema;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Restriction;
import org.opengis.feature.type.Type;
import org.opengis.util.GenericName;

public class FeatureCollectionTypeImpl extends FeatureTypeImpl implements FeatureCollectionType {
	protected final Collection<FeatureType> MEMBERTYPES;

	public FeatureCollectionTypeImpl( GenericName name, Collection<FeatureType> memberTypes, Schema schema, Type defaultGeom) {
		super(name, schema, defaultGeom);
		MEMBERTYPES = memberTypes;
	}
	public FeatureCollectionTypeImpl(GenericName name, Collection<FeatureType> memberTypes, Schema schema, Type defaultGeom, Set<Restriction> restrictions, ComplexType superType, boolean isAbstract){
		super( name, schema, defaultGeom, restrictions, superType, isAbstract );
		MEMBERTYPES = memberTypes;
	}
	public Collection<FeatureType> getMemberType() {
		return MEMBERTYPES;
	}
}
