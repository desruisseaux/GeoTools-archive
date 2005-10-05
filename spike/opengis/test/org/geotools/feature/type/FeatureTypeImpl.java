package org.geotools.feature.type;

import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.filter.Filter;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;

public class FeatureTypeImpl extends ComplexTypeImpl implements FeatureType {
	protected GeometryType DEFAULT;

	public FeatureTypeImpl(String name, Descriptor schema, GeometryType defaultGeom ) {
		this( new QName( name ), schema, defaultGeom );
	}	
	public FeatureTypeImpl(QName name, Descriptor schema, GeometryType defaultGeom ) {
		super(name, schema, true, Feature.class, false, null);
		DEFAULT = defaultGeom;
	}
	public FeatureTypeImpl(QName name, Descriptor schema, GeometryType defaultGeom, Set<Filter> restrictions, FeatureType superType, boolean isAbstract){
		super(name, schema, true, Feature.class, false, restrictions, superType, isAbstract );
		DEFAULT = defaultGeom;
	}
	public GeometryType getDefaultGeometry() {
		return DEFAULT;
	}
}
