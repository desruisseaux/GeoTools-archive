package org.geotools.feature.type;

import java.util.Set;

import org.opengis.feature.schema.Schema;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Restriction;
import org.opengis.feature.type.Type;
import org.opengis.util.GenericName;

public class FeatureTypeImpl extends ComplexTypeImpl implements FeatureType {
	protected Type DEFAULT;

	public FeatureTypeImpl(String name, Schema schema, Type defaultGeom ) {
		this( new GenericName( name ), schema, defaultGeom );
	}	
	public FeatureTypeImpl(GenericName name, Schema schema, Type defaultGeom ) {
		super(name, schema, true, null, false, null);
		DEFAULT = defaultGeom;
	}
	public FeatureTypeImpl(GenericName name, Schema schema, Type defaultGeom, Set<Restriction> restrictions, ComplexType superType, boolean isAbstract){
		super(name, schema, true, null, false, restrictions, superType, isAbstract );
	}
	public Type getDefaultGeometry() {
		return DEFAULT;
	}
}
